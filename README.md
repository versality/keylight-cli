# KeyLight CLI

A simple command-line interface to control Elgato Key Light devices using Babashka.

## Prerequisites

- [Babashka](https://github.com/babashka/babashka#installation)
- An Elgato Key Light device on your network

## Installation

### Nix

If you're using Nix/NixOS, you can install keylight-cli directly from nixpkgs:

```bash
nix-env -iA nixpkgs.keylight-cli
```

Or add it to your NixOS configuration:

```nix
environment.systemPackages = with pkgs; [
  keylight-cli
];
```

### Manual Installation

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
keylight <ip-address> [on|off]
```

### Examples

Toggle the light (switch between on and off):
```bash
keylight 192.168.1.123
```

Turn the light on:
```bash
keylight 192.168.1.123 on
```

Turn the light off:
```bash
keylight 192.168.1.123 off
