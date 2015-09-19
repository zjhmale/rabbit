(ns rabbit.cota)

(defn ->int
  ([s] (->int s nil))
  ([s default]
   (try
     (cond
       (string? s) (Integer/parseInt s)
       (instance? Number s) (.intValue s)
       :else default)
     (catch Exception _ default))))