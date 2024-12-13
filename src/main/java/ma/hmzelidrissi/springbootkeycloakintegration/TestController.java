package ma.hmzelidrissi.springbootkeycloakintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

  @GetMapping("/admin/test")
  public String adminTest() {
    return "Admin endpoint working!";
  }

  @GetMapping("/user/test")
  public String userTest() {
    return "User endpoint working!";
  }

  @GetMapping("/employee/test")
  public String employeeTest() {
    return "Employee endpoint working!";
  }
}
