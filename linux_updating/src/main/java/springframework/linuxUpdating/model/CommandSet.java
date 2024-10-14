package springframework.linuxupdating.model;

public class CommandSet {
  private String command;
  private boolean isShownInTerminal;

  public CommandSet(){
  }

  public CommandSet(String command, boolean isShownInTerminal){
    this.command = command;
    this.isShownInTerminal = isShownInTerminal;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public boolean getIsShownInTerminal() {
    return isShownInTerminal;
  }

  public void setShownInTerminal(boolean isShownInTerminal) {
    this.isShownInTerminal = isShownInTerminal;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((command == null) ? 0 : command.hashCode());
    result = prime * result + (isShownInTerminal ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CommandSet other = (CommandSet) obj;
    if (command == null) {
      if (other.command != null)
        return false;
    } else if (!command.equals(other.command))
      return false;
    if (isShownInTerminal != other.isShownInTerminal)
      return false;
    return true;
  }
}
