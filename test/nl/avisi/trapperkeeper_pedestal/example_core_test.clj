(ns nl.avisi.trapperkeeper-pedestal.example-core-test
  (:require [clojure.test :refer :all]
            [nl.avisi.trapperkeeper-pedestal.example-core :refer :all]))

(deftest hello-test
  (testing "says hello to caller"
    (is (= "Hello, foo!" (hello "foo")))))
