(ns chera.kazoeru
  (:require [chera.num-converter :refer [west-arabic->japanese]]
            [goog.dom :as gdom]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce state (r/atom {:number "" :counter "匹"}))

(defn change-state [key val] (swap! state #(-> % (assoc key (-> val .-target .-value)))))

(defn kazoeru []
  (let [{:keys [number counter] :as current-state} @state
        jp-number (or (west-arabic->japanese number) "invalid")]
    [:div {:class "h-screen p-6 bg-neutral-900"}
     [:h1  {:class "mb-0 text-xl text-slate-100"} "数えましょう！"]
     [:h2 {:class "mb-2 text-xs text-neutral-400"} "これどうやっていうの？"]
     [:div {:class "flex pl-2 py-1 mb-3 bg-neutral-800"}
      [:input {:class "w-full my-auto outline-none text-slate-100 bg-transparent"
               :type "number"
               :value (str number) :placeholder "何匹？"
               :on-change #(change-state :number %)}]
      [:button {:class "mx-2 my-auto w-3 h-3"}
       [:svg {:class "fill-slate-100"
              :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 24 24"}
        [:use {:href "./img/xmark.svg#xmark"}]]]
      [:button {:class "mx-2 my-auto w-6 h-6"}
       [:svg {:class "fill-slate-100" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 24 24" :font-size "24" :overflow "visible"}
        [:text {:x "50%" :y "55%"
                :dominant-baseline "middle" :text-anchor "middle"} counter]]]]
     [:h2 {:class "text-lg text-slate-100"} jp-number]]))

;; the Edge
(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [kazoeru] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))