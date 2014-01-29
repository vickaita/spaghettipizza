(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   [dommy.macros :refer [node]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.dom.forms :as forms]
            [goog.events :as evt]
            [goog.net.XhrIo :as xhr]
            [goog.net.WebSocket]
            [goog.history.Html5History]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [dommy.core]
            [vickaita.channels :refer [event websocket]]
            [vickaita.console :refer [log]]
            [pizza.toolbar :as toolbar]
            [pizza.ajax :as ajax]
            [pizza.svg :as svg]
            [pizza.pizza :as pzz]
            [pizza.easel :as easel]
            [pizza.spaghetti :as skins :refer [create-topping add-point!]]
            [pizza.command :refer [exec]]))

(enable-console-print!)

(defn- get-pizza-hash
  []
  ;; TODO: Can I get this information straight from the "navigation" event and
  ;; eliminate this function entirely?
  (-> (.-search (.-location js/document))
      (.split "=")
      (aget 1)))

(defn app-view
  [app owner]
  (om/component
    (html [:div#site {:class (when (:show-toolbar? app) "show-toolbar")}
           (om/build toolbar/toolbar (:toolbar app))
           [:div#page
            [:header#masthead
             [:a#menu-control
              {:on-click (fn [e]
                           (doto e .preventDefault .stopPropagation)
                           (om/update! app assoc :show-toolbar?
                                       (not (:show-toolbar? @app))))}]
             [:h1 "Spaghetti Pizza"]]
            (om/build easel/easel app)]
           [:footer [:p "Created by Vick Aita"]]])))

(defn -main
  []
  (let [;; TODO: goog.history.Html5History is pretty annoying and doesn't fit
        ;; very well with a functional style. I should either use the native
        ;; HTML5 methods or wrap it.
        history
        (goog.history.Html5History.
          js/window
          (let [tt #js {}]
            (set! (.-createUrl tt)
                  (fn [token _ _] token))
            (set! (.-retrieveToken tt)
                  (fn [path-prefix location]
                    (.substr (.-pathname location)
                             (count path-prefix))))
            tt)
          ;; XXX: For some reason this tagged literal is breaking in
          ;; advanced compilation. That's why I'm doing that crazy let
          ;; block above. Honestly, I don't know why this isn't working.
          ;#js {:createUrl (fn [token _ _] token)
          ;     :retrieveToken (fn [path-prefix location]
          ;                      (.substr (.-pathname location)
          ;                               (count path-prefix)))}
          )

        commands
        (chan)

        app-state
        (atom {:image-url nil
               :image {:status :empty}
               :width 512
               :height 512
               :crust (easel/create-irregular-circle [256 256] 227)
               :sauce (easel/create-irregular-circle [256 256] 210)
               :strokes []
               :show-toolbar? false
               :toolbar {:groups [{:name "Pasta"
                                   :tools [{:id :spaghetti :name "Spaghetti"}
                                           {:id :linguini :name "Linguini"}
                                           {:id :ziti :name "Ziti"}]}]
                         :tool :spaghetti
                         :command-channel commands}
               :tool :spaghetti})]

    (doto history
      (evt/listen "navigate"
                  #(swap! app-state assoc
                          :image-url
                          (let [pizza-hash (get-pizza-hash)]
                            (when (> (count pizza-hash) 0)
                              (str "/pizza/" (get-pizza-hash) ".png")))))
      (.setUseFragment false)
      (.setEnabled true))

    ;;;;; TODO: This should also be fired whenever the viewport is resized.
    ;;;#_(easel/adjust-size! svg-elem)

    ;;;#_(evt/listen (dom/getElement "clean") "click"
    ;;;            (fn [e]
    ;;;              (toolbar/hide!)
    ;;;              (.setToken history (str (.-origin js/location) "/"))
    ;;;              (easel/update! easel (get-pizza-hash))))

    ;;;;; XXX: This seems to be breaking noodle drawing on mobile, so disabled
    ;;;;; until I have time to investigate.
    ;;;#_(evt/listen (dom/getElement "page") "click"
    ;;;              #(when (toolbar/visible?)
    ;;;                 (do (.preventDefault %)
    ;;;                     (.stopPropagation %)
    ;;;                     (toolbar/hide!))))

    (go (while true
          (let [command (<! commands)]
            (prn command)
            (swap! app-state exec command))))

    (om/root app-state app-view (.-body js/document))))

(evt/listen js/document "DOMContentLoaded" -main)
#_(repl/connect "http://ui:9000/repl")
