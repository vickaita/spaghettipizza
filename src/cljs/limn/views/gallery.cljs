(ns limn.views.gallery
  (:require [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn gallery
  [app owner]
  (om/component
    (html
      [:div#image-wrapper
       (cond
         (:image-loading? app) [:p "Loading ..."]
         (:image-url app) [:img {:src (:image-url app)}]
         :else nil)])))
