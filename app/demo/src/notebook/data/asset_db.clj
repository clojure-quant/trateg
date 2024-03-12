(ns notebook.data.asset-db
  (:require
   [ta.db.asset.db :as db]
   [ta.db.asset.symbollist :refer [load-lists-full]]
   [ta.import.provider.kibot-http.assets :as kibot-http]))

(def asset-lists
  [;crypto
   "crypto"
   "bybit" ; auto-generated from bybit
   ; currency
   "currency-spot"
   ; futures
   "futures-kibot"
   ; bonds
   "bonds"
   ; mutualfunds
   "fidelity-select"
   ; stocks/etf
   "commodity-industry"
   "commodity-sector"
   "equity-region"
   "equity-region-country"
   "equity-sector-industry"
   "equity-style"
   "test"
   ])

(def asset-list-directory "../resources/symbollist/")

(def asset-lists-filenames
  (map #(str asset-list-directory % ".edn") asset-lists))

(defn add-lists-to-db [filenames]
  (let [asset-detail-seq (load-lists-full filenames)]
    (doall (map db/add asset-detail-seq))))

(defn add-assets []
  (add-lists-to-db asset-lists-filenames)
  (kibot-http/import-kibot-links "forex")
  :assets-added-to-db)

(comment
  asset-lists-filenames
  (add-assets)


 ; 
  )