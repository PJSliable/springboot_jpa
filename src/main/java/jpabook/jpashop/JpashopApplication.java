package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {

//		Hello hello = new Hello();
//		hello.setData("Hello");
//		String data = hello.getData();
//		System.out.println("data = " + data);

		SpringApplication.run(JpashopApplication.class, args);
	}


	@Bean
	Hibernate5Module hibernate5Module() {
		//지연 로딩의 경우 무시하는 역할
//		return new Hibernate5Module();

		//지연 로딩의 순서를 강제로 앞당겨 강제 출력
		//엔티티를 그대로 노출하는 방식이기에 엔티티가 바뀌면 다 바뀌어서 유지 보수가 어려워짐
		//사용하지 않는 정보들까지 출력하여 성능의 저하가 발생
		//Hibernate5Module를 사용하기보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법
		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}

}








