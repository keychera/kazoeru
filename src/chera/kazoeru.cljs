(ns chera.kazoeru
  (:require
   [goog.dom :as gdom]
   [reagent.dom :as rdom]))

(defn kazoeru []
  [:h1 "数えましょう！"])

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