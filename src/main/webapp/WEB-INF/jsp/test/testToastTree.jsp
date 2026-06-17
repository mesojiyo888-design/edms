<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ToastTree Test</title>

    <style>
        #treeDiv {
            width: 400px;
            min-height: 400px;
            border: 1px solid #ddd;
            padding: 10px;
            text-align: left;
        }

        .tree-button-area {
            margin-bottom: 10px;
            text-align: left;
        }

        .tree-button-area button {
            padding: 6px 10px;
            cursor: pointer;
        }

        #selectedValue {
            width: 400px;
            height: 40px;
            display: block;
            margin-bottom: 10px;
            resize: none;
        }

        .modal-backdrop {
            display: none;
            position: fixed;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.4);
            z-index: 9999;
            align-items: center;
            justify-content: center;
        }

        .modal-box {
            width: 320px;
            background: #fff;
            padding: 20px;
            border-radius: 6px;
            text-align: left;
        }

        .modal-title {
            font-weight: bold;
            margin-bottom: 10px;
        }

        .modal-buttons {
            text-align: right;
            margin-top: 15px;
        }

        .modal-buttons button {
            margin-left: 5px;
        }

        #nodeNameInput {
            width: 100%;
            box-sizing: border-box;
            padding: 5px;
        }

        #removeNodeText {
            margin-top: 10px;
            font-weight: bold;
        }
    </style>
</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">

<form name="form1" id="form1" method="POST" onsubmit="return false;">

    <div class="tree-button-area">
        <button type="button" id="addNodeBtn">노드 추가</button>
        <button type="button" id="addChildNodeBtn">하위 노드 추가</button>
        <button type="button" id="removeNodeBtn">노드 제거</button>
        <button type="button" id="getDataBtn">데이터추출</button>
    </div>

    <textarea id="selectedValue" readonly></textarea>

    <div id="treeDiv" class="tui-tree-wrap"></div>

    <!-- 서버 저장 시 사용할 JSON -->
    <input type="hidden" id="treeJson" name="treeJson"/>

    <!-- 노드 추가 모달 -->
    <div id="addNodeModal" class="modal-backdrop">
        <div class="modal-box">
            <div class="modal-title">노드 추가</div>

            <input type="text" id="nodeNameInput" placeholder="노드 이름을 입력하세요">

            <div class="modal-buttons">
                <button type="button" id="addConfirmBtn">확인</button>
                <button type="button" id="addCancelBtn">취소</button>
            </div>
        </div>
    </div>

    <!-- 노드 제거 모달 -->
    <div id="removeNodeModal" class="modal-backdrop">
        <div class="modal-box">
            <div class="modal-title">노드 제거</div>

            <div>선택한 노드를 삭제하시겠습니까?</div>
            <div id="removeNodeText"></div>

            <div class="modal-buttons">
                <button type="button" id="removeConfirmBtn">확인</button>
                <button type="button" id="removeCancelBtn">취소</button>
            </div>
        </div>
    </div>

</form>

<!--
    tui-tree.js, tui-context-menu.js는 이미 다른 곳에서 지정했다고 했으므로
    여기에는 ToastTree 공통 JS만 둔다.
-->

<script>
    $(document).ready(function() {
        /****************** tree 생성 start ******************/

        var treeData = [
            {
                text: 'rootA',
                children: [
                    {text: 'sub-A1'},
                    {text: 'sub-A2'},
                    {
                        text: 'sub-A3',
                        children: [
                            {
                                text: 'sub-A3-1',
                                children: [
                                    {
                                        text: 'sub-A3-1-1',
                                        children: [
                                            {
                                                text: 'sub-A3-1-1-1',
                                                children: [
                                                    {text: 'sub-A3-1-1-1-1'}
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                text: 'rootB',
                state: 'closed',
                children: [
                    {text: 'sub-B1'},
                    {text: 'sub-B2'},
                    {text: 'sub-B3'}
                ]
            }
        ];

        var options = {
            useContextMenu: true,

            /*
             * true  : 최대 뎁스 제한 사용
             * false : 뎁스 제한 없음
             */
            useDepthLimit: true,

            /*
             * 최대 6뎁스까지 허용
             */
            maxDepth: 6,

            /*
             * 노드/버튼 외 클릭 시 선택 해제
             */
            deselectOnOutsideClick: true,

            treeOptions: {
                nodeDefaultState: 'opened'
            },

            /*
             * 우클릭 메뉴
             */
            menuData: [
                { title: '노드 추가', command: 'addNode' },
                { title: '하위 노드 추가', command: 'addChildNode' },
                { title: '노드 제거', command: 'removeNode' }
            ]
        };

        ToastTree.init('treeDiv', treeData, options);

        /****************** tree 생성 end ******************/


        /****************** tree 버튼 연결 start ******************/

        ToastTree.bindButtons('treeDiv', {
            selectedValue: '#selectedValue',

            addNodeBtn: '#addNodeBtn',
            addChildNodeBtn: '#addChildNodeBtn',
            removeNodeBtn: '#removeNodeBtn',

            addNodeModal: '#addNodeModal',
            removeNodeModal: '#removeNodeModal',

            nodeNameInput: '#nodeNameInput',
            removeNodeText: '#removeNodeText',

            addConfirmBtn: '#addConfirmBtn',
            addCancelBtn: '#addCancelBtn',

            removeConfirmBtn: '#removeConfirmBtn',
            removeCancelBtn: '#removeCancelBtn',

            hiddenData: '#treeJson'
        });

        /****************** tree 버튼 연결 end ******************/


        /****************** 버튼 동작 start ******************/

        $('#getDataBtn').click(function(event) {
            event.preventDefault();

            var allData = ToastTree.getData('treeDiv');
            var selectedData = ToastTree.getSelectedData('treeDiv');

            console.log('전체 트리 데이터:', allData);
            console.log('선택 노드 데이터:', selectedData);
            console.log('hidden treeJson:', $('#treeJson').val());
        });

        /****************** 버튼 동작 end ******************/
    });
</script>

</body>
</html>