(ns ta.trade.roundtrip.core
  (:require
   [de.otto.nom.core :as nom]
   [tech.v3.dataset :as tds]
   [ta.trade.roundtrip.roundtrip :refer [add-performance]]
   [ta.trade.roundtrip.metrics :refer [calc-roundtrip-metrics]]
   [ta.trade.roundtrip.nav.metrics :refer [calc-nav-metrics]]
   [ta.trade.roundtrip.nav.grouped :refer [grouped-nav]]
   [ta.trade.roundtrip.validation :refer [validate-roundtrips]]))

(defn metrics-impl [roundtrip-ds]
  (let [vr (validate-roundtrips (tds/mapseq-reader roundtrip-ds))]
    (if (nom/anomaly? vr)
      vr
      (let [roundtrip-ds (add-performance roundtrip-ds)
            rt-metrics (calc-roundtrip-metrics roundtrip-ds)
            nav-metrics (calc-nav-metrics roundtrip-ds)
            nav-ds (grouped-nav roundtrip-ds)]
        {:roundtrip-ds roundtrip-ds
         :metrics {:roundtrip rt-metrics
                   :nav nav-metrics}
         :nav-ds nav-ds}))))

(defn metrics [roundtrip-ds]
  (try
    (metrics-impl roundtrip-ds)
    (catch Exception ex
      (nom/fail ::viz-calc {:message "metrics calc exception!"
                            :location :metrics
                            :ex ex ; (ex-data ex)
                            }))))