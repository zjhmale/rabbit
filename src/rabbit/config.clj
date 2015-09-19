(ns rabbit.config
  (:require [rabbit.cota :refer :all])
  (:use [environ.core :only [env]]))

;;rabbitmq default webconsole port -> 15672
(def rabbitmq-host (env :bearychat-rabbitmq-host "localhost"))
(def rabbitmq-port (->int (env :bearychat-rabbitmq-port "5672") 5672))
(def rabbitmq-username (env :bearychat-rabbitmq-username "guest"))
(def rabbitmq-password (env :bearychat-rabbitmq-password "guest"))
(defonce rabbitmq-thread-number (->int
                                  (env :rabbitmq-thread-number
                                       (str
                                         (inc (* 2 (.availableProcessors (Runtime/getRuntime))))))))

