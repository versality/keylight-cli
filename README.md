# KeyLight CLI

A simple command-line interface to control Elgato Key Light devices using Babashka.

## Prerequisites

- [Babashka](https://github.com/babashka/babashka#installation)
- An Elgato Key Light device on your network

## Installation

1. Clone this repository:
```bash
git clone https://github.com/versality/keylight-cli
cd keylight-cli
```

2. Make the script executable (optional):
```bash
chmod +x keylight.bb
```

## Usage

```bash
bb keylight.bb <ip-address> [on|off]
```

### Examples

Toggle the light (switch between on and off):
```bash
bb keylight.bb 192.168.1.123
```

Turn the light on:
```bash
bb keylight.bb 192.168.1.123 on
```

Turn the light off:
```bash
bb keylight.bb 192.168.1.123 off
```

## How It Works

The script communicates with the Elgato Key Light's HTTP API (port 9123) to control the device. It can:
- Query the current state of the light
- Turn the light on or off
- Toggle the light's state

## Dependencies

- babashka.http-client
- cheshire (for JSON parsing)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Acknowledgments

- Elgato for providing the Key Light API
- Babashka project for making Clojure scripting accessible
