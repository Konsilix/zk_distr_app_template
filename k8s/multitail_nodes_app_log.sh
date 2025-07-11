#!/usr/bin/env bash

multitail \
  -l "kubectl exec -it zkapp-0 -- tail -f /logs/app.log" \
  -l "kubectl exec -it zkapp-1 -- tail -f /logs/app.log" \
  -l "kubectl exec -it zkapp-2 -- tail -f /logs/app.log"