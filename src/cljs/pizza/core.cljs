(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [goog.history.Html5History]
            [goog.dom]
            [goog.dom.ViewportSizeMonitor]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [pizza.toolbar :as toolbar]
            [pizza.easel :as easel]
            [pizza.command :refer [exec]]))

(defn -main
  []
  (let [commands (chan)

        history
        (goog.history.Html5History.
          js/window
          (let [tt #js {}]
            (set! (.-createUrl tt)
                  (fn [token _ _] token))
            (set! (.-retrieveToken tt)
                  (fn [path-prefix location]
                    (.substr (.-pathname location)
                             (count path-prefix)))) tt)
          ;; XXX: For some reason this tagged literal is breaking in
          ;; advanced compilation. That's why I'm doing that crazy let
          ;; block above. Honestly, I don't know why this isn't working.
          ;#js {:createUrl (fn [token _ _] token)
          ;     :retrieveToken (fn [path-prefix location]
          ;                      (.substr (.-pathname location)
          ;                               (count path-prefix)))}
          )

        sizeMonitor (goog.dom.ViewportSizeMonitor.)

        app-state
        (let [size (goog.dom.getViewportSize)
              w (.-width sizeMonitor)
              h (.-height sizeMonitor)]
          (prn [w h])
          (atom {:commands commands
                 :history history
                 :debug true
                 :image-url nil
                 :image-loading? false
                 :easel-width w
                 :easel-height h
                 :viewport-width 512
                 :viewport-height 512
                 :scale-by (/ (min w h) 512)
                 :granularity 5
                 :pizza {:crust (easel/create-irregular-circle [256 256] 227)
                         :sauce (easel/create-irregular-circle [256 256] 210)}
                 :strokes []
                 :show-toolbar? false
                 :toolbar {:groups [#_{:name "Test"
                                       :tools [{:id :edit :name "Edit"}]}
                                    {:name "Pasta"
                                     :tools [{:id :spaghetti :name "Spaghetti"}
                                             {:id :linguini :name "Linguini"}
                                             {:id :ziti :name "Ziti"}]}
                                    {:name "Cheese"
                                     :tools [{:id :ricotta :name "Ricotta"}]}
                                    {:name "Herbs"
                                     :tools [{:id :basil :name "Basil"}]}]}
                 :tool :spaghetti}))
        
        app-view
        (fn [app owner]
          (om/component
            (html
              [:div#site {:class (when (:show-toolbar? app) "show-toolbar")}
               (om/build toolbar/toolbar
                         (om/graft {:commands (:commands app)
                                    :tool (:tool app)
                                    :groups (:groups (:toolbar app))}
                                   app))
               [:div#page
                [:header#masthead
                 [:a#menu-control
                  {:on-click (fn [e]
                               (doto e .preventDefault .stopPropagation)
                               (om/transact! app [:show-toolbar?] not))}]
                 [:h1 "Spaghetti Pizza"]]
                (om/build easel/easel app)]
               [:footer [:p "Created by Vick Aita"]]]))) ]

    ;; Main event loop of the program.
    (go (while true
          (let [command (<! commands)]
            ;(when (:debug @app-state) (prn command))
            (swap! app-state exec command))))

    ;; Monitor the URL for changes and fire [:navigate] events as appropriate.
    (doto history
      (events/listen
        "navigate"
        (fn [e]
          (.log js/console e)
          (when-let [search (.-search (.-location js/document))]
            (let [pizza-hash (-> search (.split "=") (aget 1))
                  img-url (when (> (count pizza-hash) 0)
                            (str "/pizza/" pizza-hash))]
              (swap! app-state #(-> %
                                    (assoc :image-url img-url)
                                    (assoc :image-loading? false)))))))
      (.setUseFragment false)
      (.setEnabled true))

    ;; Monitor the size of the window and update accordingly.
    (events/listen
      sizeMonitor 
      "resize"
      (fn [e]
        (when-let [size (.getSize sizeMonitor)]
          (let [w (.-width size)
                h (.-height size)]
            (put! commands [:resize w h])))))

    ;; Render the the application and update as the state changes.
    (om/root app-state app-view (.-body js/document))))

(enable-console-print!)
(.initializeTouchEvents js/React true)
(events/listen js/document "DOMContentLoaded" -main)
;(repl/connect "http://ui:9000/repl")
