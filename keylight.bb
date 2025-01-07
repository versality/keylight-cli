#!/usr/bin/env bb

(require '[babashka.http-client :as http]
         '[cheshire.core :as json])

(def usage
  "Usage: bb keylight.bb <ip-address> [on|off]
  Example: bb keylight.bb 192.168.1.123 on")

(defn get-light-state [ip]
  (try
    (-> (http/get (str "http://" ip ":9123/elgato/lights"))
        :body
        (json/parse-string true)
        (get-in [:lights 0]))
    (catch Exception e
      (println "Error connecting to light:" (ex-message e))
      (System/exit 1))))

(defn set-light-state [ip on?]
  (try
    (http/put (str "http://" ip ":9123/elgato/lights")
              {:body (json/generate-string
                       {:numberOfLights 1
                        :lights [{:on (if on? 1 0)}]})})
    (catch Exception e
      (println "Error setting light state:" (ex-message e))
      (System/exit 1))))

(defn -main [& args]
  (when (or (empty? args) (> (count args) 2))
    (println usage)
    (System/exit 1))

  (let [ip (first args)
        command (second args)
        current-state (get-light-state ip)]

    (case command
      "on"  (set-light-state ip true)
      "off" (set-light-state ip false)
      nil   (if (= 1 (:on current-state))
              (set-light-state ip false)
              (set-light-state ip true)))

    (let [new-state (get-light-state ip)]
      (println "Light is now" (if (= 1 (:on new-state)) "ON" "OFF")))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment
  (-main "192.168.0.249")
  (-main "192.168.0.249" "on")
  (-main "192.168.0.249" "off"))
