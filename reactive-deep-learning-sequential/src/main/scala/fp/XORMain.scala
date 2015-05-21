package fp

object XORMain extends App {

  //**************************************
  //      H
  //
  // I    H
  //
  //      H     O
  //
  // I    H
  //
  //      H
  //**************************************
  override def main (args: Array[String]) {

    val network = Seq[Vector[Vector[Double] => Double]](
      Vector(
        Perceptron.activation(Vector(-24.58461081841181, -57.122870841850805), _, -1.159477860560574, Neuron.sigmoid),
        Perceptron.activation(Vector(-24.687630829768242, 27.09898145911361), _, 7.360948031708151, Neuron.sigmoid),
        Perceptron.activation(Vector(-53.19737482058547, -0.3752614168803033), _, -40.119103666289355, Neuron.sigmoid),
        Perceptron.activation(Vector(-8.627833533421583, 2.616234689416054), _, 1.9910680740541433, Neuron.sigmoid),
        Perceptron.activation(Vector(-10.89544114987001, 1.1951518346473402), _, 35.36995814832137, Neuron.sigmoid)),
      Vector(
        Perceptron.activation(Vector(-52.985568497843346, -22.904824971874294, -67.1736083116406, 22.172982906414187, -53.706172957900264), _, 61.48889773699686, Neuron.sigmoid)
      )
    )

    println(s"Output ${Network.feedForward(Vector(0, 0), network).head} for input ${Vector(0, 0)}")
    println(s"Output ${Network.feedForward(Vector(0, 1), network).head} for input ${Vector(0, 1)}")
    println(s"Output ${Network.feedForward(Vector(1, 0), network).head} for input ${Vector(1, 0)}")
    println(s"Output ${Network.feedForward(Vector(1, 1), network).head} for input ${Vector(1, 1)}")
  }
}
