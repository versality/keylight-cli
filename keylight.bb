#!/usr/bin/env bb

(require '[babashka.http-client :as http]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(def usage
  "Usage: bb keylight.bb <ip-address> [COMMAND] [VALUE]

  Commands:
    on, off                # Turn light on/off
    --bright, -b [±]VALUE  # Set brightness (3-100) or adjust by ±VALUE
    --temp,   -t [±]VALUE  # Set temperature (143-344) or adjust by ±VALUE

  Examples:
    bb keylight.bb 192.168.1.123 on
    bb keylight.bb 192.168.1.123 -b 50      # Set brightness to 50%
    bb keylight.bb 192.168.1.123 -b +5      # Increase brightness by 5%
    bb keylight.bb 192.168.1.123 --temp -10 # Decrease temperature by 10")

(defn get-light-state [ip]
  (try
    (-> (http/get (str "http://" ip ":9123/elgato/lights"))
        :body
        (json/parse-string true)
        :lights
        first)
    (catch Exception e
      (println "Error connecting to light:" (ex-message e))
      (System/exit 1))))

(defn set-light-state [ip state-map]
  (try
    (let [payload {:numberOfLights 1
                   :lights         [(cond-> {}
                                      (contains? state-map :on) (assoc :on (if (:on state-map) 1 0))
                                      (:brightness state-map)   (assoc :brightness (:brightness state-map))
                                      (:temperature state-map)  (assoc :temperature (:temperature state-map)))]}]
      (http/put (str "http://" ip ":9123/elgato/lights")
                {:body (json/generate-string payload)}))
    (catch Exception e
      (println "Error setting light state:" (ex-message e))
      (System/exit 1))))

(defn parse-value [value-str]
  (when value-str
    (when-let [[_ sign digits] (re-matches #"([+-])?(\d+)" (str/trim value-str))]
      {:type  (if sign :relative :absolute)
       :value (parse-long (str sign digits))})))

(defn clamp [v min-v max-v]
  (-> v (max min-v) (min max-v) int))

(defn apply-value [current parsed-value [min-v max-v]]
  (let [new-val (if (= :absolute (:type parsed-value))
                  (:value parsed-value)
                  (+ current (:value parsed-value)))]
    (clamp new-val min-v max-v)))

(defn -main [& args]
  (when-not (<= 1 (count args) 3)
    (println usage)
    (System/exit 1))

  (let [[ip command value] args
        current-state (get-light-state ip)
        state-update (case command
                       "on"              {:on true}
                       "off"             {:on false}
                       ("--bright" "-b") (when-let [parsed (parse-value value)]
                                           {:brightness (apply-value (:brightness current-state) parsed [3 100])})
                       ("--temp" "-t")   (when-let [parsed (parse-value value)]
                                           {:temperature (apply-value (:temperature current-state) parsed [143 344])})
                       nil               {:on (not= 1 (:on current-state))}
                       (do (println "Unknown command:" command "\n" usage)
                           (System/exit 1)))]

    (when state-update
      (set-light-state ip state-update)
      (let [new-state (get-light-state ip)]
        (println (format "Light: %s | Brightness: %d%% | Temperature: %dK"
                         (if (= 1 (:on new-state)) "ON" "OFF")
                         (:brightness new-state)
                         (int (/ 1000000 (:temperature new-state)))))))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment
  (do
    (-main "192.168.0.249" "on")
    (-main "192.168.0.246" "on"))

  (do
    (-main "192.168.0.249" "off")
    (-main "192.168.0.246" "off"))

  (do
    (-main "192.168.0.249")
    (-main "192.168.0.246"))

  (do
    (-main "192.168.0.249" "-b" "50")
    (-main "192.168.0.246" "-b" "50"))

  (do
    (-main "192.168.0.249" "-b" "75")
    (-main "192.168.0.246" "-b" "75"))

  (do
    (-main "192.168.0.249" "-b" "+1")
    (-main "192.168.0.246" "-b" "+1"))

  (do
    (-main "192.168.0.249" "-b" "-1")
    (-main "192.168.0.246" "-b" "-1"))

  (do
    (-main "192.168.0.249" "-t" "250")
    (-main "192.168.0.246" "-t" "250"))

  (do
    (-main "192.168.0.249" "-t" "300")
    (-main "192.168.0.246" "-t" "300"))

  (do
    (-main "192.168.0.249" "-t" "+15")
    (-main "192.168.0.246" "-t" "+15"))

  (do
    (-main "192.168.0.249" "-t" "-20")
    (-main "192.168.0.246" "-t" "-20"))

  (do
    (get-light-state "192.168.0.249")
    (get-light-state "192.168.0.246"))

  ;; Individual light testing
  (-main "192.168.0.249" "on")
  (-main "192.168.0.246" "off")
  (get-light-state "192.168.0.249")
  (get-light-state "192.168.0.246"))
