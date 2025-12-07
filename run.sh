#!/bin/bash
export PATH="$PATH:/home/codespace/.local/share/coursier/bin"
cd "$(dirname "$0")"
sbt run
