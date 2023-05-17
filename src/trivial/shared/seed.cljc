(ns trivial.shared.seed
  (:require [trivial.shared.spec :as spec]))

(def round
  {::spec/trivials
   [#::spec{:question "How are you doing today?"
            :options
            [["Good!" true]
             ["Awful" true]
             ["Dead 💀" true]
             ["Like a boss 🤠" true]
             ["Bad bitch 💅" true]]
            :points 1
            :others-answers ["Dead 💀" "Like a boss 🤠"]}
    #::spec{:question "Do you consider yourself a smart person?"
            :options
            [["Yes 🧠" true]
             ["No 🧟" true]]
            :points 1}
    #::spec{:question "Have you ever had your heart broken?"
            :options
            [["Yes 💔" true]
             ["No ^.^" true]]
            :points 1}
    #::spec{:question "What's your favourite animals?"
            :options
            [["Cats 🐈" true]
             ["Dogs" true]
             ["Humsters" true]
             ["Pigs 🐖" true]]
            :points 1}
    #::spec{:question "Who would win the fight?"
            :options
            [["Iron Man 🔩" true]
             ["Spider Man 🕷" true]
             ["Hulk" true]
             ["Thor" true]
             ["Batman 🦇" false]]
            :points 1}
    #::spec{:question "Which is your favourite dinasaur?"
            :options
            [["Diplodoc" true]
             ["T-Rex 🦖" true]
             ["Pterodactil" true]]
            :points 1}]
   ::spec/id "default"})

(def trivial
  (first (get round ::spec/trivials)))
