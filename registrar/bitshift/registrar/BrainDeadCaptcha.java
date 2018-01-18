package bitshift.registrar;

public class BrainDeadCaptcha
{
  private static String[] challenges_ = {
    "5 + 3 - 2 = ",
    "12 - 1 + 8 = ",
    "20 / (2 + 3) = ",
    "10 / 10 = ",
    "(2 * 2) + 2 = ",
    "18 / 3 = ",
    "0 + 0 - 0 * 0 = ",
    "(27 - 3) / 2 = ",
    "5 - 2 + 3 = ",
    "17 - 17 + 17 = ",
    "1 + (8 / 2) + 7 = ",
    "(3 * 4) - 2 = ",
    "9 - 1 = ",
    "1 - 1 = ",
    "7 + 10 = ",
    "9 / 3 = ",
    "2 + 9 - 1 = ",
    "(6 / 2) * 3 = ",
    "4 + 4 + 4 = ",
    "15 / 3 = " };

  private static String[] answers_ = {
    "6",
    "19",
    "4",
    "1",
    "6",
    "6",
    "0",
    "12",
    "6",
    "17",
    "12",
    "10",
    "8",
    "0",
    "17",
    "3",
    "10",
    "9",
    "12",
    "5",
  };

  private int ix_;
  public int ix() { return ix_; }

  public BrainDeadCaptcha() throws Exception
  {
    ix_ = (int) (Math.random() * challenges_.length);
  }

  public String toString()
  {
    return challenges_[ix_] +
      "<input type=\"hidden\" name=\"qid\" value=\"" + ix_ + "\" />\n" +
      "  <input type=\"text\" name=\"answer\" maxlength=\"2\" size=\"2\" />";
  }

  public static boolean check( String qix, String response ) throws Exception
  {
    int ix = Integer.parseInt( qix );

    if (0 > ix || ix >= answers_.length)
      throw new Exception( "bad ix: " + ix );

    return answers_[ix].equals( response );
  }

}
