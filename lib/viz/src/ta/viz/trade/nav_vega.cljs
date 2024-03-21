(ns ta.viz.trade.nav-vega
  (:require
   [ui.vega :refer [vegalite]]))

; :date
; :open# :long$ :short$ :net$ 
; :pl-u :pl-r :pl-r-cum

(def width 500)

(defn nav-vega [data]
  [vegalite
   {:box :fl
    :spec {:description "Portfolio eval result."
           :data {:values data}
           :width width
           :height "500"
           :overflow true
           :box :fl
           :vconcat [{:height 300
                      :width width
                      :layer [{:mark "line"
                               :encoding {:x {:field "date" :type "temporal"}
                                          :y {:field "pl-cum", :type "quantitative"}
                                          :color {:value "red"}}}
                              {:mark "line"
                               :encoding {:x {:field "date" :type "temporal"}
                                          :y {:field "pl-r-cum", :type "quantitative"}
                                          :color {:value "blue"}}}]}
                     {:height 100
                      :width width
                      :mark "bar"
                      :encoding {:x {:field "date" :type "temporal"
                                     :axis {:labels false :description ""}}
                                 :y {:field "pl-u", :type "quantitative"}}}
                     {:height 100
                      :width width
                      :layer [{:mark "bar"
                               :encoding {:x {:field "date" :type "temporal"
                                              :axis {:labels false}}
                                          :y {:field "short$", :type "quantitative"}
                                          :color {:value "red"}}}
                              {:mark "bar"
                               :encoding {:x {:field "date" :type "temporal"
                                              :axis {:labels false}}
                                          :y {:field "long$", :type "quantitative"}
                                          :color {:value "green"}}}
                              {:mark "bar"
                               :encoding {:x {:field "date" :type "temporal"
                                              :axis {:labels false}}
                                          :y {:field "net$", :type "quantitative"}
                                          :color {:value "blue"}}}]}
                     {:height 100
                      :width width
                      :mark "bar"
                      :encoding {:x {:field "date" :type "temporal"
                                     :axis {:labels false}}
                                 :y {:field "open#", :type "quantitative"}}}]}}])

;:color {:field "symbol", :type "nominal"}

    ;:axis {;:tickCount 8
                                ;:labelAlign "left"
                                ;:labelExpr "[timeFormat(datum.value, '%b'), timeFormat(datum.value, '%m') == '01' ? timeFormat(datum.value, '%Y') : '']"
                                ;:labelOffset 4
                                ;:labelPadding -24
                                ;:tickSize 30
                                ;:gridDash {:condition {:test {:field "value" :timeUnit "month", :equal 1}, :value []} :value [2,2]}
                                ;:tickDash {:condition {:test {:field "value", :timeUnit "month", :equal 1}, :value []} :value [2,2]}
                          ;      }

(defn navs-chart [navs]
  (let [navs-with-index (map-indexed (fn [i v]
                                       {:index i
                                        :nav (:nav v)}) navs)
        navs-with-index (into [] navs-with-index)]
    [:div.w-full.h-full
     [:h1 "navs " (count navs-with-index)]
     (when (> (count navs) 0)
       [:div.w-full.h-full; {:style {:width "50vw"}}
        ;(pr-str navs-with-index)
        [nav-vega navs-with-index]])]))