import com.fwsim.Simulator
import org.scalatest._

class SimulatorSpec extends FlatSpec with Matchers {
  "A Simulator" should "return a deck with more than one card" in {
    // 5 colors; 10 cards per color
    val simulator = new Simulator()
    simulator.completeDeck.size should be (50)
  }
}
