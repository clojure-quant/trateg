(ns demo.goldly.random-experiments
  (:require [notespace.api]
            [tablecloth.api :as tablecloth]))

(require '[notespace.kinds :as kind]
         '[ta.dataset.helper :as helper]
         '[tablecloth.api :as tablecloth]
         '[tech.v3.dataset.print :as print]
         '[tech.v3.dataset :as dataset]
         '[tech.v3.datatype.datetime :as datetime]
         '[demo.goldly.experiments-helpers :as experiments-helpers])

(def datasets
  (->> (helper/random-datasets 3 1000)
       (map-indexed
        (fn [i ds]
          (-> ds
              (tablecloth/add-column :symbol i))))))

^kind/dataset
(first datasets)

["Check that the `:date` columns of all datasets are equal."]
(->> datasets
     (map :date)
     (apply =)
     assert)

(def concatenated-dataset
  (-> (apply tablecloth/concat datasets)
      experiments-helpers/add-year-and-month))

(-> concatenated-dataset
    (tablecloth/random 10))

(comment
  (-> concatenated-dataset
      (print/print-range :all)))


(-> concatenated-dataset
    (tablecloth/pivot->wider :symbol :close))

(-> concatenated-dataset
    (tablecloth/pivot->wider :symbol :close)
    tablecloth/last)

(-> concatenated-dataset
    (tablecloth/group-by :symbol))


(-> concatenated-dataset
    (tablecloth/group-by :symbol)
    (print/print-policy :repl))

(-> concatenated-dataset
    (tablecloth/group-by :symbol)
    (tablecloth/random 3)
    (print/print-policy :repl))


(-> concatenated-dataset
    (tablecloth/group-by [:symbol])
    (tablecloth/random 3)
    (print/print-policy :repl))

(-> concatenated-dataset
    (tablecloth/group-by [:symbol])
    (tablecloth/random 3)
    tablecloth/grouped?)


(-> concatenated-dataset
    (tablecloth/group-by :symbol)
    (tablecloth/aggregate {:min (fn [ds]
                                  (->> ds
                                       :close
                                       (apply min)))
                           :max (fn [ds]
                                  (->> ds
                                       :close
                                       (apply max)))}))

(-> concatenated-dataset
    (tablecloth/group-by [:symbol])
    (tablecloth/aggregate {:min (fn [ds]
                                  (->> ds
                                       :close
                                       (apply min)))
                           :max (fn [ds]
                                  (->> ds
                                       :close
                                       (apply max)))}))




(-> concatenated-dataset
    (tablecloth/group-by [:symbol :year])
    (tablecloth/aggregate {:min (fn [ds]
                                  (->> ds
                                       :close
                                       (apply min)))
                           :max (fn [ds]
                                  (->> ds
                                       :close
                                       (apply max)))}))



(-> concatenated-dataset
    (tablecloth/group-by [:symbol :year])
    (tablecloth/aggregate {:min (fn [ds]
                                  (->> ds
                                       :close
                                       (apply min)))
                           :max (fn [ds]
                                  (->> ds
                                       :close
                                       (apply max)))})
    (tablecloth/pivot->wider :symbol [:min :max]))




(-> concatenated-dataset
    (tablecloth/group-by [:symbol :year :month])
    (tablecloth/aggregate {:min (fn [ds]
                                  (->> ds
                                       :close
                                       (apply min)))
                           :max (fn [ds]
                                  (->> ds
                                       :close
                                       (apply max)))})
    (tablecloth/pivot->wider :symbol [:min :max])
    (print/print-range :all))





(-> concatenated-dataset
    (experiments-helpers/symbols-overview {:grouping-columns [:symbol :year :month]})
    (print/print-range :all))





