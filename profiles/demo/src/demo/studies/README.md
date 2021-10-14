# visual studio code

add this extensions
- calva
- kondo

# 2021 10 - bollinger strategy backtest

bollinger band strategy:
assumption is, that crossing lower-bollinger statistically means we will go up.

bollinger-event-analysis
event index: :above=true and :above-count=1
for each bollinger-evnet:
- create walk-forward window (if it is possible
- find min/max in walk-forward window 
- normalize min/max with range=bollinger-up - bollinger-down
the bollinger band is significant if range is squed

optimize for best parameter

event bollinger cross     ==> liste of event-bollinger-cross
cross-type #{:up :down}
up
down
up%
down%
diff   (up-down)
diff%  (up% - down%)

target funktion
for cross-type-up: average diff% 
for cross-type-down: (-average diff%)



## go to right directory

`cd profiles/demo`

## add credentials

create this file: profiles/demo/creds.edn - then add your secret key in this format:
{:alphavantage "YOUR-SECRET-KEY"}

## bybit import

- once:  `clj -X:bybit-import-initial`
- every 15 minutes / whenever you want to update. (missing a bar cannot happen)
 `clj -X:bybit-import-append`


## run strategy

`clj -X:bollinger-strategy`

`clj -X:bollinger-optimizer`


# ma-ma confirmation
- kuerzerer ma (1h = 4* 15 min) cross-up laengere ma. (6h = 24* 15 min) => long


# supertrend
- :up or :down
- regieme: zeitraum durchgehend gleicher wert.
- regieme period dataset.

- number regiemes
- average-bars-regieme-up + maverage-bars-regieme-up
- average next bar return (for up + down)

