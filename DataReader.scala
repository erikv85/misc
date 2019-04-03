object DataReader {
  def fileToStr(filename: String): String = {
    val src = io.Source.fromFile(filename)
    try src.mkString finally src.close()
  }

  def main(args: Array[String]) {
    val input = fileToStr("dummy-data.txt")
    val newlineIndices = input.zipWithIndex.collect {
      case ('\n', i) => i
    }
  }
}
