package springframework.linuxupdating.model;

public class CommandSet {
  private String command;
  private String alternativeCommand;
  private boolean isContinuedOrNo;
  private boolean isAskedToSayYesOrNo;

  public CommandSet(){
  }

  public CommandSet(String command, String alternativeCommand, boolean isContinuedOrNo, boolean isAskedToSayYesOrNo){
    this.command = command;
    this.alternativeCommand = alternativeCommand;
    this.isContinuedOrNo = isContinuedOrNo;
    this.isAskedToSayYesOrNo = isAskedToSayYesOrNo;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getAlternativeCommand() {
    return alternativeCommand;
  }

  public void setAlternativeCommand(String alternativeCommand) {
    this.alternativeCommand = alternativeCommand;
  }

  public boolean getIsContinuedOrNo() {
    return isContinuedOrNo;
  }

  public void setIsContinuedOrNo(boolean isContinuedOrNo) {
    this.isContinuedOrNo = isContinuedOrNo;
  }

  public boolean isAskedToSayYesOrNo() {
    return isAskedToSayYesOrNo;
  }

  public void setAskedToSayYesOrNo(boolean isAskedToSayYesOrNo) {
    this.isAskedToSayYesOrNo = isAskedToSayYesOrNo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((command == null) ? 0 : command.hashCode());
    result = prime * result + ((alternativeCommand == null) ? 0 : alternativeCommand.hashCode());
    result = prime * result + (isContinuedOrNo ? 1231 : 1237);
    result = prime * result + (isAskedToSayYesOrNo ? 1231 : 1237);
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
    if (alternativeCommand == null) {
      if (other.alternativeCommand != null)
        return false;
    } else if (!alternativeCommand.equals(other.alternativeCommand))
      return false;
    if (isContinuedOrNo != other.isContinuedOrNo)
      return false;
    if (isAskedToSayYesOrNo != other.isAskedToSayYesOrNo)
      return false;
    return true;
  }
}
