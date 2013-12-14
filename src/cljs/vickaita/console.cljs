(ns vickaita.console)

(defn log [& messages]
  (.log js/console (apply pr-str messages)))
