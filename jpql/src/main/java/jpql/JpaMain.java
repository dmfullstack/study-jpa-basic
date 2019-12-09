package jpql;

import jpql.domain.Address;
import jpql.domain.Member;
import jpql.domain.Team;
import jpql.dto.MemberDTO;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        try {

            for (int i = 0; i < 10; i++) {

                Team team = new Team();
                team.setName("team" + i);
                em.persist(team);

                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                member.changeTeam(team);

                em.persist(member);
            }

            em.flush();
            em.clear();

            // 엔티티 타입 프로젝션
            Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();

            System.out.println(findMember.getUsername());


            // 임베디드 타입 프로젝션
            em.createQuery("select o.address from Order o", Address.class)
                    .getResultList();

            // 스칼라 타입 프로젝션
            List<MemberDTO> resultList = em.createQuery("select distinct new jpql.dto.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();

            // 페이징
            List<Member> resultList1 = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();

            for (Member member : resultList1) {
                System.out.println(member);
            }

            // join
            List<Member> resultList2 = em.createQuery("select m from Member m join m.team t on t.name = 'team1'", Member.class)
                    .getResultList();

            for (Member member : resultList2) {
                System.out.println(member);
            }

            // type
            List<Member> resultList3 = em.createQuery("select m from Member m where m.type = jpql.domain.MemberType.ADMIN", Member.class)
                    .getResultList();

            for (Member member : resultList3) {
                System.out.println(member);
            }

            // case
            String query1 =
                    "select " +
                        "case when m.age <= 10 then '학생요금'" +
                        "     when m.age >= 70 then '경로요금'" +
                        "     else '일반요금'" +
                        "end " +
                    "from Member m";
            List<String> resultList4 = em.createQuery(query1, String.class).getResultList();

            for (String s : resultList4) {
                System.out.println(s);
            }

            // coalesce
            String query2 = "select coalesce(m.username, '이름 없는 회원') from Member m";
            List<String> resultList5 = em.createQuery(query2, String.class).getResultList();

            for (String s : resultList5) {
                System.out.println(s);
            }

            // nullif
            String query3 = "select NULLIF(m.username, '관리자') from Member m";
            List<String> resultList6 = em.createQuery(query3, String.class).getResultList();

            for (String s : resultList6) {
                System.out.println(s);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
