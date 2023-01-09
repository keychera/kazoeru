(ns chera.kazoeru
  (:require [chera.num-converter :refer [westarab->japanese]]
            [goog.dom :as gdom]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce state (r/atom {:number "" :counter "匹"}))

(defn change-state [key val] (swap! state #(-> % (assoc key (-> val .-target .-value)))))

(defn search-bar [number counter]
  [:div {:class "w-full flex pl-4 pr-1 py-2 mb-3 bg-neutral-800"}
   [:input {:class "flex-auto w-12 text-2xl text-slate-100 bg-transparent"
            :type "number"
            :value (str number) :placeholder "何匹？"
            :on-change #(change-state :number %)}]
   [:div {:class "flex-none w-12 grid content-center group relative"}
    [:button {:class "flex justify-center"}
     [:svg {:class "fill-slate-100 h-3 w-3" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 24 24"}
      [:use {:dominant-baseline "middle" :text-anchor "middle"  :href "./img/xmark.svg#xmark"}]]]
    [:div {:class "absolute left-2.5 top-10 text-slate-300 scale-0 group-hover:scale-100 transition-all"} "WIP"]]
   [:div {:class "flex-none w-0.5 bg-neutral-500"}]
   [:div {:class "flex-none w-12 grid content-center group relative"}
    [:button {:class "flex justify-center"}
     [:svg {:class "fill-slate-100" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 24 24" :font-size "14"}
      [:text {:x "50%" :y "55%" :dominant-baseline "middle" :text-anchor "middle"} counter]]]
    [:div {:class "absolute left-2.5 top-10 text-slate-300 scale-0 group-hover:scale-100 transition-all"} "WIP"]]])

(defn num+kanji-section [num+kanji]
  [:div {:class "w-full flex pl-4 pr-1 py-2 mb-3"}
   [:p {:class "flex-auto w-12 my-auto text-2xl text-slate-100"} num+kanji]
   [:div {:class "w-12 grid content-center group relative"}
    [:button {:class "h-fit"}
     [:svg {:class "fill-gray-500" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 24 24" :font-size "12"}
      [:text {:x "50%" :y "55%" :dominant-baseline "middle" :text-anchor "middle"} "ⓘ"]]]
    [:div {:class "absolute left-2.5 top-10 text-slate-300 scale-0 group-hover:scale-100 transition-all"} "WIP"]]])

(defn reading-section [reading]
  [:div {:class "flex pl-4 pr-1 py-2"}
   [:p {:class "flex-auto w-12 my-auto text-2xl text-slate-100"} reading]
   [:div {:class "w-14"}]])

(defn kazoeru []
  (let [{:keys [number counter]} @state
        {:keys [reading num+kanji]
         :or {reading "invalid" num+kanji ""}} (westarab->japanese number)]
    [:div {:class "h-screen pt-10 px-8 flex justify-center bg-neutral-900"}
    
     [:div {:class "max-w-2xl w-full"}
      (search-bar number counter)
      (num+kanji-section num+kanji)
      (reading-section reading)]]))

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