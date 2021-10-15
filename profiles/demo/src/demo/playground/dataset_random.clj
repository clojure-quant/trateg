
(ns demo.playground.dataset-random
  (:require
   [tech.v3.datatype.functional :as fun]
   [tablecloth.api :as tablecloth]
   [tick.alpha.api :as tick]
   [fastmath.random :as fr]
   [ta.dataset.date :refer [days-ago-instant select-rows-since]]
   [ta.warehouse.random :refer [random-dataset random-datasets]]
   [ta.dataset.returns :refer [forward-shift-col]]))


(random-dataset 3)

(-> (random-dataset 1000)
    (select-rows-since (days-ago-instant 6)))

(def ds-1 (random-dataset 1000000))

(let [date (days-ago-instant 6)]
  (-> ds-1
      (tablecloth/select-rows
       (fn [row]
         (-> row
             :date
             (tick/>= date))))
      time))

(def datasets (random-datasets 12 1000))
(count datasets)
(first datasets)

; tech.v3.dataset.FastStruct
(-> (random-dataset 1000)
    (tablecloth/rows :as-maps)
    first
    type)


