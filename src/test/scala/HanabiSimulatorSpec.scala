import com.fwsim.HanabiSimulator
import org.scalatest._

class HanabiSimulatorSpec extends FlatSpec with Matchers {
  "A HanabiSimulator" should "return a deck with more than one card" in {
    // 5 colors; 10 cards per color
    val simulator = new HanabiSimulator()
    simulator.completeDeck.size should be (50)
  }
}
