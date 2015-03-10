(ns nl.avisi.trapperkeeper-pedestal.example-core)

(defn hello
  "Say hello to caller"
  [caller]
  (format "Hello, %s!" caller))
