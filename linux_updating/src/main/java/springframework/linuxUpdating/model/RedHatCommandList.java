// package springframework.linuxupdating.model;

// import java.util.List;

// import javax.persistence.Entity;
// import javax.persistence.GeneratedValue;
// import javax.persistence.GenerationType;
// import javax.persistence.Id;

// @Entity
// public class RedHatCommandList {
//   @Id
//   @GeneratedValue(strategy = GenerationType.AUTO)
//   private Long id;

//   private List<String> commandList;

//   public RedHatCommandList(){
//   }

//   public Long getId() {
//     return id;
//   }

//   public void setId(Long id) {
//     this.id = id;
//   }

//   public List<String> getCommandList() {
//     return commandList;
//   }

//   public void setCommandList(List<String> commandList) {
//     this.commandList = commandList;
//   }

//   @Override
//   public int hashCode() {
//     final int prime = 31;
//     int result = 1;
//     result = prime * result + ((id == null) ? 0 : id.hashCode());
//     return result;
//   }

//   @Override
//   public boolean equals(Object obj) {
//     if (this == obj)
//       return true;
//     if (obj == null)
//       return false;
//     if (getClass() != obj.getClass())
//       return false;
//     RedHatCommandList other = (RedHatCommandList) obj;
//     if (id == null) {
//       if (other.id != null)
//         return false;
//     } else if (!id.equals(other.id))
//       return false;
//     return true;
//   }
// }
