<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

    <style>
        #gridList {
            width: 100%;
            height: 500px; /* 명시적인 높이값 필수 */
        }

        .grid-pagination ul.pagination {
            list-style: none;
            margin: 20px 0;
            padding: 0;
            display: flex;
            justify-content: center;
            gap: 4px;
        }

        .grid-pagination ul.pagination li a {
            display: inline-block;
            padding: 6px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            color: #333;
            text-decoration: none;
            background: #fff;
        }

        .grid-pagination ul.pagination li a:hover {
            background: #f0f0f0;
        }

        .grid-pagination ul.pagination li.active a {
            background: #3f51b5;
            color: #fff;
            border-color: #3f51b5;
        }

        .grid-pagination ul.pagination li.disabled a {
            color: #ccc;
            cursor: not-allowed;
            border-color: #eee;
        }
    </style>

</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<form name="form1" id="form1" action="${pageContext.request.contextPath}/board/insert" method="POST" onsubmit="return false;">

    <button id="btn-infinite" onsubmit="return false;">무한스크롤</button>
    <button id="btn-normal" onsubmit="return false;">일반페이징</button>
    <button id="get-data-btn" onsubmit="return false;">데이터추출</button>

    <div id="gridList"></div>

    <div class="grid-pagination" data-grid="gridList"></div>

</form>
<script>

    $(document).ready(function() {
        /****************** grid 생성 start ******************/
        var options = {
            bodyHeight: 400,
            refreshInterval: 10, // 10분마다 리로드
            pageOptions: {
                perPage: 10
            },
            isInfinite: false,  // 인피니티 스크롤 여부
            rowHeaders: ['checkbox'],
            columns: [
                { header: '이름', name: 'name', editor: 'text' },
                { header: '이메일', name: 'email', editor: 'text' },
                { header: '코드', name: 'codeId', editor: { type: 'select', options: { listItems: [] } } }, // 셀렉트(동적바인딩)
                 // 달력
                { header: '등록일', name: 'regDate',
                   /* editor: {
                            type: JQueryDatepickerEditor,
                            options: {
                                dateFormat: 'yy.mm.dd'
                            }
                        }

                    editor: {
                        type: 'datePicker',
                        options: { format: 'yyyy-MM-dd', language: 'ko' }
                    }
                    */
                    editor: {
                        type: ToastDatepickerEditor,
                        options: {
                            format: 'yyyy-MM-dd',
                            language: 'ko'
                        }
                    }
                },
                { header: '사용여부', name: 'useYn', editor: { type: 'checkbox', options: { listItems: [{text: '사용', value: 'Y'}] } } }, // 체크박스
                // 렌더러 예제: 링크 버튼 만들기
                { header: '상세보기', name: 'detail',
                    renderer: {
                        type: class {
                                    constructor(props) {
                                        const el = document.createElement('button');
                                        el.innerText = '보기';
                                        el.onclick = () => alert(props.row.name);
                                        this.el = el;
                                    }
                                    getElement() {
                                        return this.el;
                                    }
                                    render(props) {
                                        // 필요시 값 업데이트
                                    }
                                }
                    }
                },
                // 포맷터 예제: 조건부 스타일
                { header: '상태', name: 'status',
                    formatter: function(p) {
                        return p.value === 'Y' ? '<span style="color:blue">사용중</span>' : '정지';
                    }
                }
            ]
        };
        ToastGrid.init('gridList', _CONTEXT_PATH + '/board/list', 'form1', options);
        /****************** grid 생성 end ******************/

        // 외부에서 공통코드 주입
        var rawData = [
                { codeId: 'C', codeNm: '완료' },
                { codeId: 'W', codeNm: '대기' }
            ];

        ToastGrid.bindColumnData('gridList', 'codeId', rawData, "codeId", "codeNm", "select");


        // 버튼 동작들
        $('#btn-infinite').click(function() {
            event.preventDefault();
            ToastGrid.toggleMode('gridList', true, 50); // 인피니티 모드 + 50개씩
        });

        $('#btn-normal').click(function() {
            event.preventDefault();
            ToastGrid.toggleMode('gridList', false, 20); // 페이징
        });

        // 데이터 추출 버튼 예시
        $('#get-data-btn').click(function() {
            var allData = ToastGrid.getData('gridList');
            var checked = ToastGrid.getSelectedData('gridList');
            console.log("전체데이터:", allData);
            console.log("체크된데이터:", checked);
        });


        ComMsg.alert('저장되었습니다.');
        ComMsg.confirm('상신하시겠습니까?', '상신', function() { ... });
        ComMsg.success('승인 완료.');
        ComMsg.error('오류 발생.');
        // 버튼 텍스트만 변경
        ComMsg.confirm('삭제하시겠습니까?', '삭제', function() { deleteDoc(); }, {
          confirmButtonText: '삭제',
          cancelButtonText: '아니오'
        });

        // 이미지 버튼 (buttonsStyling: false)
        ComMsg.alert('저장되었습니다.', '알림', null, {
          buttonsStyling: false,
          customClass: { confirmButton: 'btn-custom-confirm' }
        });
    });
</script>
</body>
</html>
