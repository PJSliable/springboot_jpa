package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //default 값을 읽기 전용으로 설정
//@AllArgsConstructor //전체 필드를 가지고 생성자를 만듦
@RequiredArgsConstructor //final에 있는 필드만으로 생성자를 만듦
public class MemberService {

    //변경할 일이 없기에 final로 하는 것을 권장
    //컴파일 시점에 체크를 해줄 수 있음
    private final MemberRepository memberRepository;

    //생성자 인젝션
    //생성할 때 완성. 중간에 setter로 바꿀 수 없음
    //최신버전 spring에서는 autowired를 쓰지 않아도 생성자가 하나만 있는 경우에는 자동으로 인젝션 함
//    public MemberService(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

    //Setter 인젝션
    //중간에 바꿀일이 없는데 권한을 부여하기에 문제
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /**
     * 회원 가입
     */
    @Transactional //이 부분만 쓰기이므로 별도로 설정. @Transactional는 false가 기본적으로 부여됨
    public Long join(Member member) {
        //중복 회원 검증
        //매우 짧은 시간 차이로 유효성 검사를 통과할 수 있음
        //따라서 DB에서 name을 unique로 제약하는 것이 안전
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    //조회하는 곳에서는 읽기전용으로 설정하면 성능이 최적화됨
    public List<Member> findMember() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
