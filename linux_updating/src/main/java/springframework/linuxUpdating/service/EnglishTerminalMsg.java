package springframework.linuxupdating.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("english")
public class EnglishTerminalMsg implements TerminalMsg{
  @Value("${terminal.msg.checkOutput}")
  private String checkOutputMsg;

  @Override
  public String getCheckOutputMsg() {
    return checkOutputMsg;
  }
}
