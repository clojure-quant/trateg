(ns juan.study.doji
  (:require
   [tablecloth.api :as tc]
   [ta.trade.signal :refer [filter-signal]]
   [ta.algo.backtest :refer [backtest-algo]]))

;; 1. calculate algo

(def algo-spec {:calendar [:us :d]
                :type :trailing-bar
                :algo  'juan.algo.doji/doji-signal
                :asset "SPY"
                :import :kibot
                :trailing-n 1000
                :doji {:max-open-close-over-low-high 0.3
                       :volume-sma-n 30}
                :pivots {:step 10.0
                         :percentile 70}})

(def algo-spec-doji
  (merge algo-spec (:doji algo-spec)))

algo-spec-doji

(def dojis (backtest-algo :bardb-dynamic algo-spec-doji))


@dojis
;; => :_unnamed [965 13]:
;;    
;;    |   :open | :epoch |                :date |  :close |        :volume |   :high |    :low | :ticks | :asset | :open-close | :open-close-over-low-high |    :volume-sma | :doji-signal |
;;    |--------:|-------:|----------------------|--------:|---------------:|--------:|--------:|-------:|--------|------------:|--------------------------:|---------------:|--------------|
;;    | 280.740 |      0 | 2020-04-24T05:00:00Z | 283.040 | 6.34603700E+07 | 283.700 | 278.500 |      0 |    SPY |       2.300 |                0.44230769 | 6.34603700E+07 |        :flat |
;;    | 285.090 |      0 | 2020-04-27T05:00:00Z | 287.110 | 6.16133180E+07 | 288.270 | 284.620 |      0 |    SPY |       2.020 |                0.55342466 | 6.33988016E+07 |        :flat |
;;    | 291.020 |      0 | 2020-04-28T05:00:00Z | 285.770 | 8.77339870E+07 | 291.400 | 285.400 |      0 |    SPY |      -5.250 |               -0.87500000 | 6.42079222E+07 |       :short |
;;    | 291.540 |      0 | 2020-04-29T05:00:00Z | 293.190 | 1.02369673E+08 | 294.880 | 290.410 |      0 |    SPY |       1.650 |                0.36912752 | 6.55048989E+07 |        :flat |
;;    | 291.710 |      0 | 2020-04-30T05:00:00Z | 290.390 | 1.02456394E+08 | 292.235 | 288.590 |      0 |    SPY |      -1.320 |               -0.36213992 | 6.68047664E+07 |       :short |
;;    | 285.310 |      0 | 2020-05-01T05:00:00Z | 282.730 | 9.65026420E+07 | 286.040 | 281.520 |      0 |    SPY |      -2.580 |               -0.57079646 | 6.79061755E+07 |       :short |
;;    | 280.735 |      0 | 2020-05-04T05:00:00Z | 283.660 | 6.62484190E+07 | 283.900 | 279.130 |      0 |    SPY |       2.925 |                0.61320755 | 6.79991104E+07 |        :flat |
;;    | 286.640 |      0 | 2020-05-05T05:00:00Z | 286.220 | 6.68806540E+07 | 289.250 | 285.750 |      0 |    SPY |      -0.420 |               -0.12000000 | 6.81131199E+07 |        :flat |
;;    | 288.030 |      0 | 2020-05-06T05:00:00Z | 284.290 | 6.33260680E+07 | 288.460 | 283.780 |      0 |    SPY |      -3.740 |               -0.79914530 | 6.81086432E+07 |        :flat |
;;    | 287.750 |      0 | 2020-05-07T05:00:00Z | 287.700 | 6.16090750E+07 | 289.780 | 287.130 |      0 |    SPY |      -0.050 |               -0.01886792 | 6.80469333E+07 |        :flat |
;;    |     ... |    ... |                  ... |     ... |            ... |     ... |     ... |    ... |    ... |         ... |                       ... |            ... |          ... |
;;    | 496.290 |      0 | 2024-02-07T05:00:00Z | 498.050 | 5.21706320E+07 | 498.530 | 495.360 |      0 |    SPY |       1.760 |                0.55520505 | 5.80352995E+07 |        :flat |
;;    | 498.100 |      0 | 2024-02-08T05:00:00Z | 498.400 | 4.14217210E+07 | 498.710 | 497.260 |      0 |    SPY |       0.300 |                0.20689655 | 5.82706295E+07 |        :flat |
;;    | 498.860 |      0 | 2024-02-09T05:00:00Z | 501.180 | 4.77196040E+07 | 501.650 | 498.490 |      0 |    SPY |       2.320 |                0.73417722 | 5.83781744E+07 |        :flat |
;;    | 501.170 |      0 | 2024-02-12T05:00:00Z | 500.940 | 4.11409410E+07 | 503.500 | 500.240 |      0 |    SPY |      -0.230 |               -0.07055215 | 5.84458035E+07 |        :flat |
;;    | 494.510 |      0 | 2024-02-13T05:00:00Z | 494.140 | 8.85400580E+07 | 495.850 | 490.715 |      0 |    SPY |      -0.370 |               -0.07205453 | 5.88830883E+07 |       :short |
;;    | 496.790 |      0 | 2024-02-14T05:00:00Z | 498.625 | 5.49103650E+07 | 499.070 | 494.400 |      0 |    SPY |       1.835 |                0.39293362 | 5.80528972E+07 |        :flat |
;;    | 499.300 |      0 | 2024-02-15T05:00:00Z | 501.950 | 4.84382780E+07 | 502.200 | 498.795 |      0 |    SPY |       2.650 |                0.77826725 | 5.72379250E+07 |        :flat |
;;    | 501.710 |      0 | 2024-02-16T05:00:00Z | 499.490 | 5.17029070E+07 | 502.870 | 498.750 |      0 |    SPY |      -2.220 |               -0.53883495 | 5.69911584E+07 |        :flat |
;;    | 501.710 |      0 | 2024-02-16T05:00:00Z | 499.490 | 5.17029070E+07 | 502.870 | 498.750 |      0 |    SPY |      -2.220 |               -0.53883495 | 5.65653429E+07 |        :flat |
;;    | 497.720 |      0 | 2024-02-20T05:00:00Z | 496.740 | 5.57275230E+07 | 498.410 | 494.450 |      0 |    SPY |      -0.980 |               -0.24747475 | 5.64283377E+07 |        :flat |
;;    | 497.720 |      0 | 2024-02-20T05:00:00Z | 496.740 | 5.57275230E+07 | 498.410 | 494.450 |      0 |    SPY |      -0.980 |               -0.24747475 | 5.66688756E+07 |        :flat |


(filter-signal {:signal :long
                :of :doji-signal} @dojis)
;; => :_unnamed [52 13]:
;;    
;;    |  :open | :epoch |                :date | :close |        :volume |    :high |     :low | :ticks | :asset | :open-close | :open-close-over-low-high |    :volume-sma | :doji-signal |
;;    |-------:|-------:|----------------------|-------:|---------------:|---------:|---------:|-------:|--------|------------:|--------------------------:|---------------:|--------------|
;;    | 302.10 |      0 | 2020-05-27T05:00:00Z | 303.48 | 8.51989400E+07 | 303.5700 | 296.8700 |      0 |    SPY |        1.38 |                0.20597015 | 7.43983189E+07 |        :long |
;;    | 325.90 |      0 | 2020-07-31T05:00:00Z | 326.55 | 6.49287320E+07 | 326.6100 | 321.3300 |      0 |    SPY |        0.65 |                0.12310606 | 6.15039700E+07 |        :long |
;;    | 337.49 |      0 | 2020-09-14T05:00:00Z | 338.42 | 5.00949910E+07 | 340.3800 | 336.9300 |      0 |    SPY |        0.93 |                0.26956522 | 4.94611802E+07 |        :long |
;;    | 325.70 |      0 | 2020-09-21T05:00:00Z | 327.00 | 8.00852800E+07 | 327.0900 | 321.7300 |      0 |    SPY |        1.30 |                0.24253731 | 5.40726288E+07 |        :long |
;;    | 330.20 |      0 | 2020-11-02T05:00:00Z | 330.21 | 6.63173470E+07 | 332.3600 | 327.2400 |      0 |    SPY |        0.01 |                0.00195312 | 5.88968264E+07 |        :long |
;;    | 349.24 |      0 | 2020-11-05T05:00:00Z | 350.21 | 6.76368160E+07 | 352.1900 | 348.8600 |      0 |    SPY |        0.97 |                0.29129129 | 6.05857778E+07 |        :long |
;;    | 353.48 |      0 | 2020-11-10T05:00:00Z | 354.07 | 6.78939290E+07 | 355.1800 | 350.5100 |      0 |    SPY |        0.59 |                0.12633833 | 6.36458059E+07 |        :long |
;;    | 364.97 |      0 | 2020-12-21T05:00:00Z | 367.93 | 7.83251580E+07 | 378.4600 | 362.0300 |      0 |    SPY |        2.96 |                0.18015825 | 5.07402910E+07 |        :long |
;;    | 380.59 |      0 | 2021-01-08T05:00:00Z | 381.24 | 5.44968190E+07 | 381.4900 | 377.1000 |      0 |    SPY |        0.65 |                0.14806378 | 4.69724109E+07 |        :long |
;;    | 383.67 |      0 | 2021-01-25T05:00:00Z | 384.40 | 5.66306650E+07 | 384.7700 | 378.4600 |      0 |    SPY |        0.73 |                0.11568938 | 4.73577002E+07 |        :long |
;;    |    ... |    ... |                  ... |    ... |            ... |      ... |      ... |    ... |    ... |         ... |                       ... |            ... |          ... |
;;    | 437.01 |      0 | 2023-06-14T05:00:00Z | 437.18 | 7.21615690E+07 | 439.0612 | 433.5900 |      0 |    SPY |        0.17 |                0.03107179 | 6.08767636E+07 |        :long |
;;    | 432.93 |      0 | 2023-06-23T05:00:00Z | 433.21 | 6.28359020E+07 | 435.0600 | 432.4700 |      0 |    SPY |        0.28 |                0.10810811 | 6.11168282E+07 |        :long |
;;    | 439.41 |      0 | 2023-07-06T05:00:00Z | 439.67 | 6.09191120E+07 | 440.1000 | 437.0600 |      0 |    SPY |        0.26 |                0.08552632 | 6.02215001E+07 |        :long |
;;    | 454.47 |      0 | 2023-07-26T05:00:00Z | 455.51 | 5.62044990E+07 | 456.9900 | 453.3800 |      0 |    SPY |        1.04 |                0.28808864 | 5.51668022E+07 |        :long |
;;    | 448.08 |      0 | 2023-08-08T05:00:00Z | 448.75 | 5.39923360E+07 | 449.2300 | 445.2705 |      0 |    SPY |        0.67 |                0.16921328 | 5.31733800E+07 |        :long |
;;    | 438.68 |      0 | 2023-08-25T05:00:00Z | 439.95 | 8.31340220E+07 | 441.3000 | 435.0000 |      0 |    SPY |        1.27 |                0.20158730 | 5.69648902E+07 |        :long |
;;    | 426.62 |      0 | 2023-10-02T05:00:00Z | 427.36 | 7.23645290E+07 | 428.6000 | 424.4600 |      0 |    SPY |        0.74 |                0.17874396 | 6.14728390E+07 |        :long |
;;    | 426.62 |      0 | 2023-10-02T05:00:00Z | 427.36 | 7.23645290E+07 | 428.6000 | 424.4600 |      0 |    SPY |        0.74 |                0.17874396 | 6.20491465E+07 |        :long |
;;    | 419.61 |      0 | 2023-10-23T05:00:00Z | 420.45 | 7.61838640E+07 | 424.4500 | 417.8000 |      0 |    SPY |        0.84 |                0.12631579 | 7.11902729E+07 |        :long |
;;    | 472.16 |      0 | 2024-01-02T05:00:00Z | 472.60 | 7.98160980E+07 | 473.6700 | 470.4900 |      0 |    SPY |        0.44 |                0.13836478 | 5.46862277E+07 |        :long |
;;    | 467.49 |      0 | 2024-01-05T05:00:00Z | 467.96 | 6.44773710E+07 | 470.4400 | 466.4300 |      0 |    SPY |        0.47 |                0.11720698 | 5.65504932E+07 |        :long |

(filter-signal {:signal :short
                :of :doji-signal} @dojis)
;; => :_unnamed [212 13]:
;;    
;;    |  :open | :epoch |                :date | :close |        :volume |    :high |     :low | :ticks | :asset | :open-close | :open-close-over-low-high |    :volume-sma | :doji-signal |
;;    |-------:|-------:|----------------------|-------:|---------------:|---------:|---------:|-------:|--------|------------:|--------------------------:|---------------:|--------------|
;;    | 291.02 |      0 | 2020-04-28T05:00:00Z | 285.77 | 8.77339870E+07 | 291.4000 | 285.4000 |      0 |    SPY |       -5.25 |               -0.87500000 | 6.42079222E+07 |       :short |
;;    | 291.71 |      0 | 2020-04-30T05:00:00Z | 290.39 | 1.02456394E+08 | 292.2350 | 288.5900 |      0 |    SPY |       -1.32 |               -0.36213992 | 6.68047664E+07 |       :short |
;;    | 285.31 |      0 | 2020-05-01T05:00:00Z | 282.73 | 9.65026420E+07 | 286.0400 | 281.5200 |      0 |    SPY |       -2.58 |               -0.57079646 | 6.79061755E+07 |       :short |
;;    | 293.78 |      0 | 2020-05-12T05:00:00Z | 286.62 | 7.92553310E+07 | 294.2400 | 286.5500 |      0 |    SPY |       -7.16 |               -0.93107932 | 6.84665024E+07 |       :short |
;;    | 286.05 |      0 | 2020-05-13T05:00:00Z | 281.67 | 1.20191976E+08 | 287.1900 | 278.9650 |      0 |    SPY |       -4.38 |               -0.53252280 | 7.03575559E+07 |       :short |
;;    | 294.34 |      0 | 2020-05-19T05:00:00Z | 292.02 | 7.48618730E+07 | 296.2050 | 291.9500 |      0 |    SPY |       -2.32 |               -0.54524089 | 7.38549463E+07 |       :short |
;;    | 321.42 |      0 | 2020-06-10T05:00:00Z | 319.01 | 7.71886520E+07 | 322.3900 | 318.2209 |      0 |    SPY |       -2.41 |               -0.57806241 | 7.62976450E+07 |       :short |
;;    | 311.46 |      0 | 2020-06-11T05:00:00Z | 300.62 | 1.75672810E+08 | 312.1500 | 300.0100 |      0 |    SPY |      -10.84 |               -0.89291598 | 7.87410829E+07 |       :short |
;;    | 308.24 |      0 | 2020-06-12T05:00:00Z | 304.28 | 1.55167776E+08 | 309.0800 | 298.6000 |      0 |    SPY |       -3.96 |               -0.37786260 | 8.04981290E+07 |       :short |
;;    | 315.47 |      0 | 2020-06-16T05:00:00Z | 312.93 | 1.22207644E+08 | 315.6400 | 307.6700 |      0 |    SPY |       -2.54 |               -0.31869511 | 8.28002660E+07 |       :short |
;;    |    ... |    ... |                  ... |    ... |            ... |      ... |      ... |    ... |    ... |         ... |                       ... |            ... |          ... |
;;    | 472.51 |      0 | 2023-12-14T05:00:00Z | 472.00 | 8.15844760E+07 | 473.7300 | 469.2500 |      0 |    SPY |       -0.51 |               -0.11383929 | 5.52708850E+07 |       :short |
;;    | 469.49 |      0 | 2023-12-15T05:00:00Z | 469.37 | 7.90068640E+07 | 470.7000 | 468.4400 |      0 |    SPY |       -0.12 |               -0.05309735 | 5.54627660E+07 |       :short |
;;    | 473.96 |      0 | 2023-12-20T05:00:00Z | 467.97 | 7.74583080E+07 | 475.8950 | 467.8200 |      0 |    SPY |       -5.99 |               -0.74179567 | 5.53848636E+07 |       :short |
;;    | 476.49 |      0 | 2023-12-29T05:00:00Z | 475.35 | 7.54215140E+07 | 477.0300 | 473.3000 |      0 |    SPY |       -1.14 |               -0.30563003 | 5.37641806E+07 |       :short |
;;    | 470.43 |      0 | 2024-01-03T05:00:00Z | 468.73 | 7.28874420E+07 | 471.1900 | 468.1700 |      0 |    SPY |       -1.70 |               -0.56291391 | 5.55596403E+07 |       :short |
;;    | 468.30 |      0 | 2024-01-04T05:00:00Z | 467.27 | 5.91059070E+07 | 470.9600 | 467.0500 |      0 |    SPY |       -1.03 |               -0.26342711 | 5.57393430E+07 |       :short |
;;    | 477.58 |      0 | 2024-01-11T05:00:00Z | 476.32 | 6.32075260E+07 | 478.1200 | 472.2600 |      0 |    SPY |       -1.26 |               -0.21501706 | 5.89611849E+07 |       :short |
;;    | 475.25 |      0 | 2024-01-16T05:00:00Z | 474.95 | 6.24283580E+07 | 476.6098 | 473.0600 |      0 |    SPY |       -0.30 |               -0.08451180 | 5.89425907E+07 |       :short |
;;    | 487.81 |      0 | 2024-01-24T05:00:00Z | 485.32 | 6.50803220E+07 | 488.7700 | 485.0100 |      0 |    SPY |       -2.49 |               -0.66223404 | 5.92509836E+07 |       :short |
;;    | 488.62 |      0 | 2024-01-31T05:00:00Z | 482.92 | 9.21015030E+07 | 489.0813 | 482.8900 |      0 |    SPY |       -5.70 |               -0.92064671 | 5.80077685E+07 |       :short |
;;    | 494.51 |      0 | 2024-02-13T05:00:00Z | 494.14 | 8.85400580E+07 | 495.8500 | 490.7150 |      0 |    SPY |       -0.37 |               -0.07205453 | 5.88830883E+07 |       :short |

(def algo-spec-intraday {:calendar [:forex :m]
                         :algo  'juan.algo.doji/doji-signal
                         :type :trailing-bar
                         :asset "EURUSD"
                         :import :kibot-http
                         :trailing-n 100000
                         :doji {:max-open-close-over-low-high 0.3
                                :volume-sma-n 30}
                         :pivots {:step 10.0
                                  :percentile 70}})

(def algo-spec-doji-forex-intraday
  (merge algo-spec-intraday (:doji algo-spec-intraday)))

algo-spec-doji-forex-intraday

(def dojis-forex-intraday (backtest-algo :duckdb algo-spec-doji-forex-intraday))
;(def dojis-forex-intraday (backtest-algo :bardb-dynamic algo-spec-doji-forex-intraday))

(-> @dojis-forex-intraday
   (tc/unique-by :date)
   (tc/select-columns [:date :volume :close  ;bar-ds
                       :open-close-over-low-high :volume-sma ; intermediary columns
                       :doji-signal ; what we really are interested in
                        ]))
;; => :_unnamed [96517 6]:
;;    
;;    |                :date | :volume |  :close | :open-close-over-low-high | :volume-sma | :doji-signal |
;;    |----------------------|--------:|--------:|--------------------------:|------------:|--------------|
;;    | 2023-11-20T04:36:00Z |    37.0 | 1.09266 |                1.00000000 | 37.00000000 |        :flat |
;;    | 2023-11-20T04:37:00Z |    42.0 | 1.09266 |                0.33333333 | 37.16666667 |        :flat |
;;    | 2023-11-20T04:38:00Z |    44.0 | 1.09269 |                0.33333333 | 38.40000000 |        :flat |
;;    | 2023-11-20T04:39:00Z |    15.0 | 1.09267 |               -1.00000000 | 39.06666667 |        :flat |
;;    | 2023-11-20T04:40:00Z |    55.0 | 1.09258 |               -0.69230769 | 35.26666667 |       :short |
;;    | 2023-11-20T04:41:00Z |    52.0 | 1.09266 |                0.60000000 | 39.36666667 |        :flat |
;;    | 2023-11-20T04:42:00Z |    42.0 | 1.09268 |                0.20000000 | 41.53333333 |        :long |
;;    | 2023-11-20T04:43:00Z |    22.0 | 1.09270 |                0.50000000 | 40.46666667 |        :flat |
;;    | 2023-11-20T04:44:00Z |    39.0 | 1.09271 |                0.20000000 | 41.70000000 |        :flat |
;;    | 2023-11-20T04:45:00Z |    39.0 | 1.09267 |               -0.75000000 | 39.30000000 |        :flat |
;;    |                  ... |     ... |     ... |                       ... |         ... |          ... |
;;    | 2024-02-23T21:49:00Z |    20.0 | 1.08208 |               -1.00000000 | 35.76666667 |        :flat |
;;    | 2024-02-23T21:50:00Z |    45.0 | 1.08211 |                0.66666667 | 35.50000000 |        :flat |
;;    | 2024-02-23T21:51:00Z |    61.0 | 1.08210 |               -0.50000000 | 35.93333333 |       :short |
;;    | 2024-02-23T21:52:00Z |    15.0 | 1.08209 |               -0.20000000 | 35.76666667 |        :flat |
;;    | 2024-02-23T21:53:00Z |    51.0 | 1.08219 |                0.81818182 | 36.80000000 |        :flat |
;;    | 2024-02-23T21:54:00Z |   105.0 | 1.08209 |               -0.73333333 | 39.73333333 |       :short |
;;    | 2024-02-23T21:55:00Z |    37.0 | 1.08208 |                0.50000000 | 39.30000000 |        :flat |
;;    | 2024-02-23T21:56:00Z |    77.0 | 1.08195 |               -0.50000000 | 41.16666667 |       :short |
;;    | 2024-02-23T21:57:00Z |    40.0 | 1.08196 |                0.33333333 | 41.80000000 |        :flat |
;;    | 2024-02-23T21:58:00Z |    66.0 | 1.08185 |               -0.68421053 | 42.50000000 |       :short |
;;    | 2024-02-23T21:59:00Z |    68.0 | 1.08175 |                0.38461538 | 43.96666667 |        :flat |

(-> 
(filter-signal {:signal :short
                :of :doji-signal} @dojis-forex-intraday)
(tc/unique-by :date))
;; => :_unnamed [22073 13]:
;;    
;;    |   :open | :epoch |                :date |  :close | :volume |   :high |    :low | :ticks | :asset | :open-close | :open-close-over-low-high |  :volume-sma | :doji-signal |
;;    |--------:|-------:|----------------------|--------:|--------:|--------:|--------:|-------:|--------|------------:|--------------------------:|-------------:|--------------|
;;    | 1.08506 |      0 | 2023-11-17T05:16:00Z | 1.08504 |    36.0 | 1.08511 | 1.08504 |      0 | EURUSD |    -0.00002 |               -0.28571429 |  36.00000000 |       :short |
;;    | 1.08510 |      0 | 2023-11-17T05:20:00Z | 1.08501 |    79.0 | 1.08511 | 1.08500 |      0 | EURUSD |    -0.00009 |               -0.81818182 |  38.03333333 |       :short |
;;    | 1.08500 |      0 | 2023-11-17T05:21:00Z | 1.08496 |    76.0 | 1.08505 | 1.08496 |      0 | EURUSD |    -0.00004 |               -0.44444444 |  46.53333333 |       :short |
;;    | 1.08491 |      0 | 2023-11-17T05:24:00Z | 1.08486 |    55.0 | 1.08491 | 1.08485 |      0 | EURUSD |    -0.00005 |               -0.83333333 |  51.36666667 |       :short |
;;    | 1.08499 |      0 | 2023-11-17T05:31:00Z | 1.08494 |    85.0 | 1.08500 | 1.08489 |      0 | EURUSD |    -0.00005 |               -0.45454545 |  50.90000000 |       :short |
;;    | 1.08491 |      0 | 2023-11-17T05:33:00Z | 1.08490 |    96.0 | 1.08496 | 1.08486 |      0 | EURUSD |    -0.00001 |               -0.10000000 |  62.20000000 |       :short |
;;    | 1.08510 |      0 | 2023-11-17T05:46:00Z | 1.08509 |    58.0 | 1.08514 | 1.08509 |      0 | EURUSD |    -0.00001 |               -0.20000000 |  54.90000000 |       :short |
;;    | 1.08493 |      0 | 2023-11-17T05:52:00Z | 1.08483 |    88.0 | 1.08493 | 1.08479 |      0 | EURUSD |    -0.00010 |               -0.71428571 |  79.20000000 |       :short |
;;    | 1.08545 |      0 | 2023-11-17T05:59:00Z | 1.08531 |   185.0 | 1.08557 | 1.08531 |      0 | EURUSD |    -0.00014 |               -0.53846154 | 138.23333333 |       :short |
;;    | 1.08557 |      0 | 2023-11-17T06:09:00Z | 1.08551 |    85.0 | 1.08561 | 1.08551 |      0 | EURUSD |    -0.00006 |               -0.60000000 |  49.33333333 |       :short |
;;    |     ... |    ... |                  ... |     ... |     ... |     ... |     ... |    ... |    ... |         ... |                       ... |          ... |          ... |
;;    | 1.08162 |      0 | 2024-02-22T16:33:00Z | 1.08149 |   192.0 | 1.08162 | 1.08142 |      0 | EURUSD |    -0.00013 |               -0.65000000 | 152.46666667 |       :short |
;;    | 1.08149 |      0 | 2024-02-22T16:34:00Z | 1.08148 |   175.0 | 1.08155 | 1.08146 |      0 | EURUSD |    -0.00001 |               -0.11111111 | 148.36666667 |       :short |
;;    | 1.08149 |      0 | 2024-02-22T16:35:00Z | 1.08144 |   227.0 | 1.08149 | 1.08141 |      0 | EURUSD |    -0.00005 |               -0.62500000 | 153.80000000 |       :short |
;;    | 1.08141 |      0 | 2024-02-22T16:44:00Z | 1.08137 |   162.0 | 1.08145 | 1.08135 |      0 | EURUSD |    -0.00004 |               -0.40000000 | 137.13333333 |       :short |
;;    | 1.08136 |      0 | 2024-02-22T16:45:00Z | 1.08118 |   156.0 | 1.08139 | 1.08116 |      0 | EURUSD |    -0.00018 |               -0.78260870 | 146.83333333 |       :short |
;;    | 1.08117 |      0 | 2024-02-22T16:47:00Z | 1.08102 |   198.0 | 1.08118 | 1.08098 |      0 | EURUSD |    -0.00015 |               -0.75000000 | 148.86666667 |       :short |
;;    | 1.08101 |      0 | 2024-02-22T16:48:00Z | 1.08096 |   203.0 | 1.08102 | 1.08088 |      0 | EURUSD |    -0.00005 |               -0.35714286 | 156.36666667 |       :short |
;;    | 1.08132 |      0 | 2024-02-22T16:54:00Z | 1.08129 |   190.0 | 1.08134 | 1.08128 |      0 | EURUSD |    -0.00003 |               -0.50000000 | 185.43333333 |       :short |
;;    | 1.08144 |      0 | 2024-02-22T17:05:00Z | 1.08124 |   165.0 | 1.08145 | 1.08123 |      0 | EURUSD |    -0.00020 |               -0.90909091 | 151.76666667 |       :short |
;;    | 1.08125 |      0 | 2024-02-22T17:06:00Z | 1.08122 |   196.0 | 1.08125 | 1.08119 |      0 | EURUSD |    -0.00003 |               -0.50000000 | 139.00000000 |       :short |
;;    | 1.08132 |      0 | 2024-02-22T17:11:00Z | 1.08131 |   168.0 | 1.08134 | 1.08123 |      0 | EURUSD |    -0.00001 |               -0.09090909 | 144.26666667 |       :short |

(-> (filter-signal {:signal :long
                    :of :doji-signal} @dojis-forex-intraday)
    (tc/unique-by :date))
;; => :_unnamed [8158 13]:
;;    
;;    |   :open | :epoch |                :date |  :close | :volume |   :high |    :low | :ticks | :asset | :open-close | :open-close-over-low-high |  :volume-sma | :doji-signal |
;;    |--------:|-------:|----------------------|--------:|--------:|--------:|--------:|-------:|--------|------------:|--------------------------:|-------------:|--------------|
;;    | 1.08505 |      0 | 2023-11-17T05:17:00Z | 1.08505 |    42.0 | 1.08506 | 1.08500 |      0 | EURUSD |     0.00000 |                0.00000000 |  36.20000000 |        :long |
;;    | 1.08506 |      0 | 2023-11-17T05:18:00Z | 1.08506 |    49.0 | 1.08507 | 1.08504 |      0 | EURUSD |     0.00000 |                0.00000000 |  37.63333333 |        :long |
;;    | 1.08489 |      0 | 2023-11-17T05:37:00Z | 1.08491 |    66.0 | 1.08494 | 1.08485 |      0 | EURUSD |     0.00002 |                0.22222222 |  61.20000000 |        :long |
;;    | 1.08494 |      0 | 2023-11-17T05:39:00Z | 1.08494 |    61.0 | 1.08497 | 1.08494 |      0 | EURUSD |     0.00000 |                0.00000000 |  54.46666667 |        :long |
;;    | 1.08494 |      0 | 2023-11-17T05:48:00Z | 1.08494 |    77.0 | 1.08495 | 1.08490 |      0 | EURUSD |     0.00000 |                0.00000000 |  54.63333333 |        :long |
;;    | 1.08497 |      0 | 2023-11-17T05:50:00Z | 1.08499 |   128.0 | 1.08499 | 1.08489 |      0 | EURUSD |     0.00002 |                0.20000000 |  66.96666667 |        :long |
;;    | 1.08526 |      0 | 2023-11-17T06:26:00Z | 1.08526 |    74.0 | 1.08527 | 1.08521 |      0 | EURUSD |     0.00000 |                0.00000000 |  67.66666667 |        :long |
;;    | 1.08521 |      0 | 2023-11-17T06:35:00Z | 1.08522 |    83.0 | 1.08528 | 1.08520 |      0 | EURUSD |     0.00001 |                0.12500000 |  59.13333333 |        :long |
;;    | 1.08517 |      0 | 2023-11-17T06:46:00Z | 1.08517 |    73.0 | 1.08519 | 1.08512 |      0 | EURUSD |     0.00000 |                0.00000000 |  54.13333333 |        :long |
;;    | 1.08429 |      0 | 2023-11-17T07:16:00Z | 1.08434 |   157.0 | 1.08436 | 1.08415 |      0 | EURUSD |     0.00005 |                0.23809524 | 134.90000000 |        :long |
;;    |     ... |    ... |                  ... |     ... |     ... |     ... |     ... |    ... |    ... |         ... |                       ... |          ... |          ... |
;;    | 1.08142 |      0 | 2024-02-22T14:18:00Z | 1.08146 |   266.0 | 1.08150 | 1.08135 |      0 | EURUSD |     0.00004 |                0.26666667 | 247.90000000 |        :long |
;;    | 1.08122 |      0 | 2024-02-22T14:27:00Z | 1.08123 |   288.0 | 1.08130 | 1.08116 |      0 | EURUSD |     0.00001 |                0.07142857 | 268.86666667 |        :long |
;;    | 1.08128 |      0 | 2024-02-22T14:29:00Z | 1.08129 |   280.0 | 1.08132 | 1.08119 |      0 | EURUSD |     0.00001 |                0.07692308 | 248.56666667 |        :long |
;;    | 1.08097 |      0 | 2024-02-22T15:16:00Z | 1.08099 |   532.0 | 1.08104 | 1.08087 |      0 | EURUSD |     0.00002 |                0.11764706 | 514.50000000 |        :long |
;;    | 1.08108 |      0 | 2024-02-22T15:51:00Z | 1.08109 |   347.0 | 1.08117 | 1.08104 |      0 | EURUSD |     0.00001 |                0.07692308 | 286.96666667 |        :long |
;;    | 1.08118 |      0 | 2024-02-22T15:54:00Z | 1.08118 |   371.0 | 1.08119 | 1.08101 |      0 | EURUSD |     0.00000 |                0.00000000 | 281.00000000 |        :long |
;;    | 1.08117 |      0 | 2024-02-22T15:55:00Z | 1.08118 |   342.0 | 1.08126 | 1.08112 |      0 | EURUSD |     0.00001 |                0.07142857 | 306.16666667 |        :long |
;;    | 1.08167 |      0 | 2024-02-22T16:26:00Z | 1.08167 |   203.0 | 1.08175 | 1.08164 |      0 | EURUSD |     0.00000 |                0.00000000 | 142.13333333 |        :long |
;;    | 1.08167 |      0 | 2024-02-22T16:27:00Z | 1.08167 |   170.0 | 1.08173 | 1.08164 |      0 | EURUSD |     0.00000 |                0.00000000 | 160.20000000 |        :long |
;;    | 1.08141 |      0 | 2024-02-22T16:43:00Z | 1.08142 |   168.0 | 1.08153 | 1.08141 |      0 | EURUSD |     0.00001 |                0.08333333 | 126.53333333 |        :long |
;;    | 1.08141 |      0 | 2024-02-22T17:09:00Z | 1.08144 |   154.0 | 1.08148 | 1.08127 |      0 | EURUSD |     0.00003 |                0.14285714 | 148.66666667 |        :long |

(-> (filter-signal {:signal :short
                    :of :doji-signal} @dojis-forex-intraday)
    (tc/unique-by :date))