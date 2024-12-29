package springframework.linuxupdating.model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Accessor {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String globalIpAddr;
  private String localIpAddr;
  private String hostName;
  private String loginId;
  private String passPhrase;
  private String rootPassword;
  private String secretKey;

  public Accessor(){
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getGlobalIpAddr() {
    return globalIpAddr;
  }

  public void setGlobalIpAddr(String globalIpAddr) {
    this.globalIpAddr = globalIpAddr;
  }

  public String getLocalIpAddr() {
    return localIpAddr;
  }

  public void setLocalIpAddr(String localIpAddr) {
    this.localIpAddr = localIpAddr;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public String getPassPhrase() {
    return passPhrase;
  }

  public void setPassPhrase(String passPhrase) {
    this.passPhrase = passPhrase;
  }

  public String getRootPassword() {
    return rootPassword;
  }

  public void setRootPassword(String rootPassword) {
    this.rootPassword = rootPassword;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Accessor other = (Accessor) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}