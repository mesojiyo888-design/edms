package egovframework.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class EgovUserDetails implements UserDetails {

    private static final Logger log = LoggerFactory.getLogger(EgovUserDetails.class);
    private static final long serialVersionUID = 1L;

    private String userId;
    private Set<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>();

    // 전체 역할 병합 플래그 (역할 무관하게 "이 사람이 결재권 있나" 체크용)
    private boolean approvalYn;
    private boolean docYn;
    private boolean sendYn;
    private boolean selectYn;

    // roleId -> 해당 role이 가진 PERM_* 목록 (예: "A" -> {PERM_APPROVAL, PERM_DOC, PERM_SEND})
    private Map<String, Set<String>> authList = new LinkedHashMap<String, Set<String>>();

    public EgovUserDetails(String userId) {
        this.userId = userId;
    }

    // ==========================================
    // 현재 로그인 사용자 꺼내는 정적 헬퍼
    // ==========================================
    public static EgovUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof EgovUserDetails) {
            return (EgovUserDetails) authentication.getPrincipal();
        }

        return null;
    }

    // ==========================================
    // 권한 병합 (쿼리 결과 리스트를 받아 authorities + authList 동시 구성)
    // ==========================================
    public void applyPermissions(List<Map<String, String>> permissionList) {
        for(Map<String, String> perm : permissionList) {
            log.debug("@@@ EgovUserDetails.applyPermissions : perm = {}", perm.toString());

            String roleId = perm.get("role_id");
            log.debug("@@@ EgovUserDetails.applyPermissions : roleId = {}", roleId);

            boolean approvalYn = "Y".equals(perm.get("approval_yn"));
            boolean docYn = "Y".equals(perm.get("doc_yn"));
            boolean sendYn = "Y".equals(perm.get("send_yn"));
            boolean selectYn = "Y".equals(perm.get("select_yn"));

            if(approvalYn) this.approvalYn = true;
            if(docYn) this.docYn = true;
            if(sendYn) this.sendYn = true;
            if(selectYn) this.selectYn = true;

            if(roleId != null) {
                this.authorities.add(new SimpleGrantedAuthority(roleId));

                Set<String> permSet = new LinkedHashSet<String>();
                if(approvalYn) permSet.add("PERM_APPROVAL");
                if(docYn) permSet.add("PERM_DOC");
                if(sendYn) permSet.add("PERM_SEND");
                if(selectYn) permSet.add("PERM_SELECT");

                this.authList.put(roleId, permSet);

                for(String tmpPerm : permSet) {
                    this.authorities.add(new SimpleGrantedAuthority(tmpPerm));
                }
            }
        }
    }

    // ==========================================
    // role별 권한 체크 - Java/JSTL 공용
    // 사용: hasAuthList("A", "PERM_APPROVAL")
    // ==========================================
    public boolean hasAuthList(String roleId, String perm) {
        Set<String> permSet = this.authList.get(roleId);
        return permSet != null && permSet.contains(perm);
    }

    public Map<String, Set<String>> getAuthList() {
        return authList;
    }

    public boolean isApprovalYn() {
        return approvalYn;
    }

    public boolean isDocYn() {
        return docYn;
    }

    public boolean isSendYn() {
        return sendYn;
    }

    public boolean isSelectYn() {
        return selectYn;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}