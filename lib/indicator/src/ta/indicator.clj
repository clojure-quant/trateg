(ns ta.indicator
  (:require
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.datatype.statistics :as stats]
   [tech.v3.dataset.rolling :as r :refer [rolling mean]]
   [tablecloth.api :as tc]
   [ta.indicator.helper :refer [indicator]]
   [ta.indicator.signal :refer [upward-change downward-change]]
   [ta.helper.ds :refer [has-col]]
   [ta.math.series :refer [gauss-summation]])
  (:import [clojure.lang PersistentQueue]))

(defn prior
  "this does not work, as it always returns the current value."
  [col]
  (let [ds (tc/dataset {:col col})]
    (:prior (rolling ds {:window-size 1
                         :relative-window-position :left}
                     {:prior (r/last :col)}))))

(defn sma
  "Simple moving average"
  [{:keys [n]
    :or {n 100}}
   col]
  (let [ds (tc/dataset {:col col})]
    (:sma (rolling ds {:window-size n
                       :relative-window-position :left}
                   {:sma (mean :col)}))))

(defn sma2
  "sma indicator, that does not produce any value until
   minimum p bars are present."
  [p]
  (indicator
   [values (volatile! PersistentQueue/EMPTY)
    sum (volatile! 0.0)]
   (fn [x]
     (vswap! sum + x)
     (vswap! values conj x)
     (when (> (count @values) p)
       (vswap! sum - (first @values))
       (vswap! values pop))
     (when (= (count @values) p)
       (/ @sum p)))))

;; https://www.investopedia.com/ask/answers/071414/whats-difference-between-moving-average-and-weighted-moving-average.asp
(defn- wma-f
  "series with asc index order (not reversed like in pine script)"
  [series len norm]
  (let [sum (reduce + (for [i (range len)] (* (nth series i) (+ i 1))))
        ; TODO use vector functions. fix reify
        ;sum (dfn/+
        ;      (dfn/* series
        ;             (range 1 (inc (count series)))))
        ]
    (double (/ sum norm))))

(defn wma
  "Weighted moving average"
  [n col]
  (let [ds (tc/dataset {:col col})
        norm (gauss-summation n)]
    (:wma (r/rolling ds {:window-type :fixed
                         :window-size n
                         :relative-window-position :left}
                     {:wma {:column-name [:col]
                            :reducer (fn [window]
                                       (wma-f window n norm))}}))))

(defn- calc-ema-idx
  "EMA-next = (cur-close-price - prev-ema) * alpha + prev-ema"
  [alpha]
  (indicator
   [prev-ema (volatile! nil)]
   (fn [x]
     (let [r (if @prev-ema
               (-> (- x @prev-ema)
                   (* alpha)
                   (+ @prev-ema))
               x)]
       (vreset! prev-ema r)
       r))))

(defn ema
  "Exponential moving average"
  [n col]
  (let [alpha (/ 2 (inc n))]
    (into [] (calc-ema-idx alpha) col)))

(defn mma
  "Modified moving average"
  [n col]
  (let [alpha (/ 1 n)]
    (into [] (calc-ema-idx alpha) col)))

(defn macd
  ([col] (macd {:n 12 :m 26} col))
  ([{:keys [n m]} col]
   (let [ema-short (ema n col)
         ema-long (ema m col)]
     (dfn/- ema-short ema-long))))

(defn rsi
  "Relative strength index"
  [n col]
  (let [gain (upward-change col)
        loss (downward-change col)
        mma-gain (mma n gain)
        mma-loss (mma n loss)
        len (count gain)]
    (dtype/make-reader
     :float64 len
     (if (= 0.0 (mma-loss idx))
       (if (= 0.0 (mma-gain idx)) 0 100)
       (- 100 (/ 100
                 (+ 1 (/ (mma-gain idx)
                         (mma-loss idx)))))))))

(defn hlc3
  "input: bar-ds with (:close :high :low) columns
   output: (high+low+close) / 3"
  [bar-ds]
  (assert (has-col bar-ds :low) "hlc3 needs :low column in bar-ds")
  (assert (has-col bar-ds :high) "hlc3 needs :high column in bar-ds")
  (assert (has-col bar-ds :close) "hlc3 needs :close column in bar-ds")
  (let [low (:low bar-ds)
        high (:high bar-ds)
        close (:close bar-ds)
        hlc3 (dfn// (dfn/+ low high close) 3.0)]
    hlc3))

(defn tr
  "input: bar-ds with (:low :high) columns
   output: (high-low)"
  [bar-ds]
  (assert (has-col bar-ds :low) "tr needs :low column in bar-ds")
  (assert (has-col bar-ds :high) "tr needs :high column in bar-ds")
  (let [low (:low bar-ds)
        high (:high bar-ds)
        hl (dfn/- high low)]
    hl))

(defn atr [{:keys [n]} bar-ds]
  (assert n "atr needs :n option")
  (let [ds (tc/add-column bar-ds :tr (tr bar-ds))]
    (:atr (rolling ds {:window-size n
                       :relative-window-position :left}
                   {:atr (mean :tr)}))))

(defn add-atr [opts bar-ds]
  (tc/add-column bar-ds :atr (atr opts bar-ds)))

(defn carry-forward
  "carries forward the last non-nil-non-nan value of vector x.
   carries the value forward indefinitely."
  [x]
  (let [p (volatile! Double/NaN); 
        n (count x)]
    ; dtype/clone is essential. otherwise on large datasets, the mapping will not
    ; be done in sequence, which means that the stateful mapping function will fail.
    (dtype/clone
     (dtype/make-reader
      :float64 n
      (let [v (x idx)]
        (if (or (nil? v) (NaN? v))
          @p
          (vreset! p v)))))))

(defn carry-forward-for
  "carries forward the last non-nil-non-nan value of vector x.
   carries the value a maximum of n bars."
  [n x]
  (let [p (volatile! Double/NaN)
        i (volatile! 0)
        l (count x)
        set-value (fn [v]
                    (vreset! i 0)
                    (vreset! p v))]
    ; dtype/clone is essential. otherwise on large datasets, the mapping will not
    ; be done in sequence, which means that the stateful mapping function will fail.
    (dtype/clone
     (dtype/make-reader
      :float64 l
      (let [v (x idx)]
        (if (or (nil? v) (NaN? v))
          (if (< @i n)
            (do (vswap! i inc)
                @p)
            Double/NaN)
          (set-value v)))))))

(comment
  (def ds
    (tc/dataset [{:open 100 :high 120 :low 90 :close 100}
                 {:open 100 :high 120 :low 90 :close 101}
                 {:open 100 :high 140 :low 90 :close 102}
                 {:open 100 :high 140 :low 90 :close 104}
                 {:open 100 :high 140 :low 90 :close 104}
                 {:open 100 :high 160 :low 90 :close 106}
                 {:open 100 :high 160 :low 90 :close 107}
                 {:open 100 :high 160 :low 90 :close 110}]))

  (into [] sma2 [4 5 6 7 8 6 5 4 3])

  (tr ds)

  (atr {:n 2} ds)
  (add-atr {:n 5} ds)

  (sma {:n 2} ds)

  (carry-forward [nil Double/NaN 1.0 nil nil -1.0 2.0 nil])

  (carry-forward-for 1 [1.0 nil nil -1.0 2.0 nil])
  (carry-forward-for 1 [1.0 Double/NaN nil -1.0 2.0 nil])

; 
  )



