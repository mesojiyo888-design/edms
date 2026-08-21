<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style>

        .tui-tree-wrap
        {
            box-sizing   : border-box;
            display      : inline-block;
            min-width    : 280px;
            max-width    : 480px;
            width        : auto;
            padding      : 16px 12px;
            background   : #ffffff;
            border       : 1px solid #e5e7eb;
            border-radius: 10px;
            box-shadow   : 0 2px 8px rgba(0, 0, 0, 0.06);
            font-family  : 'Malgun Gothic', sans-serif;
            font-size    : 14px;
            color        : #1f2937;
            text-align   : left;
            overflow-x   : hidden;   /* 불필요한 가로 스크롤 제거 */
            overflow-y   : visible;
        }

        .tui-tree-wrap *
        {
            box-sizing: border-box;
        }

        /* 텍스트가 길어도 가로 스크롤 대신 자연스럽게 줄바꿈 */
        .tui-tree-wrap .tui-tree-text
        {
            white-space  : normal;
            word-break   : break-all;
        }

        /* 트리 내부 li 한 줄 */
        .tui-tree-wrap li
        {
            line-height: 30px;
        }

        /* 노드 텍스트 (아이콘 옆 이름) */
        .tui-tree-wrap .tui-tree-text
        {
            padding      : 3px 10px;
            border-radius: 6px;
            cursor       : pointer;
            transition   : background-color 0.12s ease;
        }

        .tui-tree-wrap .tui-tree-text:hover
        {
            background-color: #f3f4f6;
        }

        /* 선택된 노드 (Selectable 기능의 selectedClassName과 동일해야 함) */
        .tui-tree-wrap .tui-tree-selected
        {
            background-color: #e8f0fe !important;
            color           : #1a56db;
            font-weight     : 600;
        }

        /* 펼침/접힘 화살표 버튼 */
        .tui-tree-wrap .tui-tree-toggleBtn
        {
            color      : #9ca3af;
            font-size  : 12px;
            margin-right: 2px;
        }

        /* 하위 항목이 있는(폴더) 노드 - 폴더 아이콘 느낌으로 진하게 */
        .tui-tree-wrap li.tui-tree-hasChild > .tui-tree-content > .tui-tree-text
        {
            font-weight: 600;
            color      : #374151;
        }

        .tui-tree-wrap li.tui-tree-hasChild > .tui-tree-content > .tui-tree-text::before
        {
            content     : "📁";
            margin-right: 6px;
            font-size   : 13px;
        }

        /* 하위 항목이 없는(파일/leaf) 노드 */
        .tui-tree-wrap li:not(.tui-tree-hasChild) > .tui-tree-content > .tui-tree-text::before
        {
            content     : "📄";
            margin-right: 6px;
            font-size   : 12px;
            opacity     : 0.7;
        }

        /* -------------------------------------------------------------------
           컨텍스트메뉴 (ComTree.js의 _showContextMenu가 그리는 <ul class="com-tree-context-menu">)
           ------------------------------------------------------------------- */
        .com-tree-context-menu
        {
            font-family: 'Malgun Gothic', sans-serif;
        }

        .com-tree-context-menu li:hover
        {
            background-color: #f3f4f6 !important;
        }

        /* -------------------------------------------------------------------
           입력 모달 (ComTree.js의 _showInputModal이 그리는 .com-tree-modal-box)
           ------------------------------------------------------------------- */
        .com-tree-modal-box input:focus
        {
            outline   : none;
            border-color: #3182f6 !important;
            box-shadow: 0 0 0 2px rgba(49, 130, 246, 0.15);
        }
    </style>
</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<form action="${pageContext.request.contextPath}/board/insert" method="POST">
    <button onclick="ComTree.expandAll('menuTree');return false;">전체 열기</button>
    <button onclick="ComTree.collapseAll('menuTree');return false;">전체 닫기</button>
    <div id="menuTree"></div>
    <button onclick="ComTree.triggerAdd('menuTree');return false;">등록</button>
    <button onclick="ComTree.triggerEdit('menuTree');return false;">수정</button>
    <button onclick="ComTree.triggerDelete('menuTree');return false;">삭제</button>
</form>
<script>

    $(document).ready(function(){
        initFolderTree();
    });


var flatList =
[
    { menuId: 'M001', upperMenuId: null,   menuNm: '문서관리',     menuOrder: 1, menuUrl: '/doc/docList' },
    { menuId: 'M002', upperMenuId: 'M001', menuNm: '기안',        menuOrder: 1, menuUrl: '/doc/draftList' },
    { menuId: 'M003', upperMenuId: 'M001', menuNm: '결재함',      menuOrder: 2, menuUrl: '/doc/approvalList' },
    { menuId: 'M004', upperMenuId: null,   menuNm: '시스템관리',   menuOrder: 2, menuUrl: '/system/systemList' },
    { menuId: 'M005', upperMenuId: 'M004', menuNm: '사용자관리',   menuOrder: 1, menuUrl: '/system/userList' },
    { menuId: 'M006', upperMenuId: 'M004', menuNm: '권한관리',     menuOrder: 2, menuUrl: '/system/roleList' }
];

    function initFolderTree() {

    ComTree.register('menuTree', 'menuTree', {
            flatData: flatList,
            fieldMap: {
                id      : 'menuId',
                parentId: 'upperMenuId',
                text    : 'menuNm',
                order   : 'menuOrder'
            },
            rootParentId: null,
            rootLabel   : '전체 메뉴',
            maxDepth : 3,
            onSelect  : onSelect,
            contextMenu : {
                onAddChild: onAddChild,
                onEdit    : onEdit,
                onDelete  : onDelete
            }
        });

        ComTree.selectFirstNode('menuTree');

    }

    function onSelect(nodeId, nodeData) {
        console.log('선택된 노드 ID:', nodeId);
        console.log('선택된 노드 데이터:', JSON.stringify(nodeData));
        //console.log('선택된 노드 데이터 menuUrl:', nodeData.raw.menuUrl);
    }

    function onAddChild(nodeId, nodeData, rollback) {
    console.log('onAddChild 호출됨. nodeId:', nodeId, 'nodeData:', JSON.stringify(nodeData));
        /*
        $.ajax({
            url        : '/api/folder/save',
            method     : 'POST',
            contentType: 'application/json',
            data       : JSON.stringify({
                name: nodeData.text
                // parentId는 필요 시 nodeId 기준으로 서버에서 조회하거나
                // ComTree 쪽에 부모 customId 조회 함수를 추가해서 넘기면 됩니다.
            })
        }).done(function(response) {
            if(!response.success) {
                ComMsg.alert(response.message || '등록에 실패했습니다.', 'error');
                rollback();      // 트리에 추가됐던 노드를 다시 제거
                return;
            }

            // 서버가 발급한 진짜 PK를 트리 노드에 반영
            ComTree.editNodeData('docFolderTree', nodeId, { id: response.data.id });
            ComMsg.toast('등록되었습니다.');
        }).fail(function() {
            ComMsg.alert('서버 통신 중 오류가 발생했습니다.', 'error');
            rollback();          // 트리에 추가됐던 노드를 다시 제거
        });
        */
    }

    // -----------------------------------------------------------
    // 이름 수정 완료 후 호출됨 (트리 텍스트는 이미 바뀐 상태)
    // -----------------------------------------------------------
    function onEdit(nodeId, nodeData, rollback) {
    /*
        $.ajax({
            url        : '/api/folder/update',
            method     : 'PUT',
            contentType: 'application/json',
            data       : JSON.stringify(
            {
                id  : nodeData.id,
                name: nodeData.text
            })
        }).done(function(response) {
            if(!response.success) {
                ComMsg.alert(response.message || '수정에 실패했습니다.', 'error');
                rollback();      // 트리 텍스트를 수정 전으로 되돌림
                return;
            }

            ComMsg.toast('수정되었습니다.');
        }).fail(function(){
            ComMsg.alert('서버 통신 중 오류가 발생했습니다.', 'error');
            rollback();          // 트리 텍스트를 수정 전으로 되돌림
        });
        */
    };

    // -----------------------------------------------------------
    // 삭제 완료 후 호출됨 (트리에서는 이미 사라진 상태)
    // -----------------------------------------------------------
    function onDelete(nodeId, nodeData, rollback) {
        /*
        $.ajax({
            url   : '/api/folder/' + nodeData.id,
            method: 'DELETE'
        }).done(function(response) {
            if(!response.success) {
                ComMsg.alert(response.message || '삭제에 실패했습니다.', 'error');
                rollback();      // 삭제됐던 노드를 원래 위치(부모 하위)에 복원
                return;
            }

            ComMsg.toast('삭제되었습니다.');
        }).fail(function() {
            ComMsg.alert('서버 통신 중 오류가 발생했습니다.', 'error');
            rollback();          // 삭제됐던 노드를 원래 위치(부모 하위)에 복원
        });
        */
    }

</script>
</body>
</html>
