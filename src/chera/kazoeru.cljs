(ns chera.kazoeru
  (:require
   [goog.dom :as gdom]
   [reagent.dom :as rdom]
   [reagent.core :as r]))

(defonce state (r/atom {:number "" :counter "匹"}))

(defn change-state [key val] (swap! state #(-> % (assoc key (-> val .-target .-value)))))

(defn kazoeru []
  (let [{:keys [number counter] :as current-state} @state]
    [:div [:h1 "数えましょう！"]
     [:div
      [:input {:type "text"
               :value (str number)
               :placeholder "何匹？"
               :on-change #(change-state :number %)}]
      [:button {:class "w-6 h-6 px-1.5"}
       [:img {:class "h-4" :src "./img/xmark.svg" :alt "clear-text"}]]
      [:button {:class "w-6 h-6"}
       [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :stroke "#000" :font-size "20" :overflow "visible"}
        [:text {:font-family "Roboto Condensed" :x "0" :y "24"} counter]]]]
     [:h2 {} current-state]]))

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