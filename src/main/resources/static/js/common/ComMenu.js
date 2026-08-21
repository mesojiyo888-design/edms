var ComMenu = (function() {
    'use strict';

    var _menuList = [];
    var _$navContainer = null;
    var _$lnbContainer = null;
    var _$breadcrumb = null;
    var _$pageTitle = null;

    /**
     * ComMenu 초기화 진입점.
     * 각 영역 셀렉터와 메뉴 데이터를 받아 내부 상태에 저장한 뒤 최초 렌더링을 수행한다.
     * @param navSelector GNB ul 셀렉터
     * @param lnbSelector LNB ul 셀렉터
     * @param breadcrumbSelector 브레드크럼 ol 셀렉터
     * @param titleSelector 페이지 타이틀 셀렉터
     * @param menuData 세션 메뉴 목록(JSON 파싱된 배열)
     */
    function fnInit(navSelector, lnbSelector, breadcrumbSelector, titleSelector, menuData) {
        _$navContainer = $(navSelector);
        _$lnbContainer = $(lnbSelector);
        _$breadcrumb = $(breadcrumbSelector);
        _$pageTitle = $(titleSelector);
        _menuList = menuData || [];
        fnRender();
    }

    /**
     * 현재 URL 기준으로 선택된 메뉴와 상위 체인을 계산한 뒤
     * GNB, LNB, 브레드크럼, 페이지 타이틀을 한 번에 다시 그린다.
     */
    function fnRender() {
        var currentPath = fnGetCurrentPath();
        var currentMenu = fnFindCurrentMenu(currentPath);
        var currentChain = currentMenu ? fnBuildMenuChain(currentMenu) : [];
        var currentTopMenu = currentChain.length > 0 ? currentChain[0] : null;

        fnRenderNav(currentMenu, currentChain);
        fnRenderLnb(currentMenu, currentTopMenu);
        fnRenderBreadcrumb(currentChain);
        fnRenderPageTitle(currentMenu);
    }

    /**
     * 현재 요청 경로(location.pathname)와 menuUrl이 일치하는 메뉴를 찾는다.
     * 2뎁스에 URL이 걸려있는 경우도 여기서 바로 매칭된다.
     * @param currentPath 현재 페이지 경로
     * @returns 일치하는 메뉴 객체, 없으면 null
     */
    function fnFindCurrentMenu(currentPath) {
        for(var i = 0; i < _menuList.length; i++) {
            if(_menuList[i].menuUrl === currentPath) {
                return _menuList[i];
            }
        }
        return null;
    }

    /**
     * menuId로 메뉴 객체를 조회한다. upperMenuId 체인 탐색, 클릭 시 메뉴 조회,
     * 외부 API 호출(fnMoveToMenu) 등에 공통으로 쓰인다.
     * @param menuId 조회할 메뉴 ID (null/undefined면 즉시 null 반환)
     * @returns 일치하는 메뉴 객체, 없으면 null
     */
    function fnFindMenuById(menuId) {
        if(!menuId) {
            return null;
        }
        for(var i = 0; i < _menuList.length; i++) {
            if(_menuList[i].menuId === menuId) {
                return _menuList[i];
            }
        }
        return null;
    }

    /**
     * 주어진 부모 메뉴(menuId)의 첫 번째 자식 메뉴를 찾는다.
     * _menuList는 menuOrd 순으로 이미 정렬되어 내려오므로, 순서상 첫 매칭 항목이 곧 첫 자식이다.
     * @param menuId 부모 메뉴 ID
     * @returns 첫 번째 자식 메뉴 객체, 없으면 null
     */
    function fnFindFirstChild(menuId) {
        for(var i = 0; i < _menuList.length; i++) {
            if(_menuList[i].upperMenuId === menuId) {
                return _menuList[i];
            }
        }
        return null;
    }

    /**
     * 메뉴가 실제로 이동해야 할 URL을 계산한다.
     * 자기 자신에게 menuUrl이 있으면 그대로 사용하고,
     * 없으면(1뎁스처럼 URL 없는 상위 메뉴) 첫 번째 자식으로 재귀 탐색하여
     * URL을 가진 최하위 노드(보통 3뎁스)까지 내려간다.
     * @param menu URL을 확인할 메뉴 객체
     * @returns 이동 가능한 URL 문자열, 자식이 전혀 없으면 'javascript:void(0)'
     */
    function fnResolveMenuUrl(menu) {
        if(menu.menuUrl) {
            return menu.menuUrl;
        }
        var firstChild = fnFindFirstChild(menu.menuId);
        if(!firstChild) {
            return 'javascript:void(0)';
        }
        return fnResolveMenuUrl(firstChild);
    }

    /**
     * 현재 메뉴부터 upperMenuId를 따라 최상위 메뉴까지 거슬러 올라가
     * Home 방향(최상위 → 현재 메뉴) 순서의 배열로 만든다.
     * 브레드크럼 및 GNB selected 판정(fnIsInChain)에 사용된다.
     * @param currentMenu 현재 선택된 메뉴 객체
     * @returns 최상위부터 현재 메뉴까지의 메뉴 배열
     */
    function fnBuildMenuChain(currentMenu) {
        var chain = [];
        var menu = currentMenu;

        while(menu) {
            chain.unshift(menu);
            menu = fnFindMenuById(menu.upperMenuId);
        }

        return chain;
    }

    /**
     * 주어진 메뉴가 현재 메뉴의 상위 체인에 포함되는지 확인한다.
     * 2뎁스 이하 화면에 있을 때 그 상위 1뎁스 GNB 항목을 selected 처리하기 위해 사용.
     * @param menu 체인 포함 여부를 확인할 메뉴
     * @param chain fnBuildMenuChain으로 만든 현재 메뉴의 상위 체인 배열
     * @returns 체인에 포함되면 true
     */
    function fnIsInChain(menu, chain) {
        return chain.some(function(chainMenu) {
            return chainMenu.menuId === menu.menuId;
        });
    }

    /**
     * GNB(1뎁스 메뉴)를 렌더링한다.
     * 현재 메뉴의 상위 체인에 포함된 항목에 selected 클래스를 부여하고,
     * 렌더링 후 클릭 이벤트를 다시 바인딩한다.
     * @param currentMenu 현재 선택된 메뉴 객체(없을 수 있음)
     * @param currentChain 현재 메뉴의 상위 체인 배열
     */
    function fnRenderNav(currentMenu, currentChain) {
        var gnbMenus = _menuList.filter(function(menu) {
            return menu.menuDepth === 1;
        });

        var html = gnbMenus.map(function(menu) {
            var isSelected = fnIsInChain(menu, currentChain);
            return '<li class="menu-item' + (isSelected ? ' selected' : '') + '"' +
                ' data-menu-id="' + menu.menuId + '">' +
                '<a href="javascript:void(0)">' + menu.menuNm + '</a>' +
                '</li>';
        }).join('');

        _$navContainer.html(html);
        fnBindNavEvents();
    }

    /**
     * LNB(2뎁스 메뉴)를 렌더링한다.
     * 현재 선택된 GNB(1뎁스) 하위에 속하는 2뎁스 메뉴만 필터링해서 보여준다.
     * 상위 GNB가 없는 경우(현재 메뉴를 못 찾은 경우) LNB는 비운다.
     * @param currentMenu 현재 선택된 메뉴 객체
     * @param currentTopMenu 현재 메뉴 체인의 최상위(1뎁스) 메뉴
     */
    function fnRenderLnb(currentMenu, currentTopMenu) {
        if(!currentTopMenu) {
            _$lnbContainer.empty();
            return;
        }

        var lnbMenus = _menuList.filter(function(menu) {
            return menu.menuDepth === 2 && menu.upperMenuId === currentTopMenu.menuId;
        });

        var html = lnbMenus.map(function(menu) {
            var isSelected = currentMenu && menu.menuId === currentMenu.menuId;
            var itemDiv = '<div class="lnb-item' + (isSelected ? ' selected' : '') + '"' +
                ' data-menu-id="' + menu.menuId + '"><tit>' + menu.menuNm + '</tit></div>';
            var jobDiv = '';

            // job div는 li/ul 없이 선택된 항목 div 바로 뒤에 형제로 붙는다.
            // (li 중첩 구조가 아니므로 클릭 버블링 걱정 없이 각자 독립적인 클릭 대상이 된다)
            if(isSelected && menu.openType === 'TAB' && menu.jobList && menu.jobList.length > 0) {
                var jobHtml = menu.jobList.map(function(job) {
                    return '<li data-job-id="' + job.jobId + '">' +
                        '<input type="checkbox" class="job-checkbox" data-job-id="' + job.jobId + '">' +
                        '<label>' + job.jobNm + '</label>' +
                        '</li>';
                }).join('');
                jobDiv = '<div class="job-list-wrap">' +
                    '<ul class="job-list">' + jobHtml + '</ul>' +
                    '<button type="button" class="job-open-btn">선택된job열기</button>' +
                    '</div>';
            }

            return itemDiv + jobDiv;
        }).join('');

        _$lnbContainer.html(html);
        fnBindLnbEvents();
    }

    /**
     * job 체크박스로 선택된 jobId 배열을 받아, 탭 하나에 한번에 연다.
     * jobId별로 탭을 따로 열지 않고, jobIds를 콤마로 묶어 하나의 URL 파라미터로 붙인다.
     * 탭 제목은 menuNm이 아니라 체크된 job들의 jobNm을 '+'로 이어붙인 문자열을 쓴다.
     * 탭 매칭 키(tabKey)도 jobIds 묶음 문자열 기준으로 만들어지므로,
     * 같은 menuId라도 선택된 job 조합이 다르면 별도 탭, 조합이 같으면 기존 탭 재사용.
     * @param menu job이 속한 LNB 메뉴 객체
     * @param jobIds 선택된 jobId 배열
     * @param tabTitle 체크된 job들의 jobNm을 '+'로 이어붙인 탭 제목
     */
    function fnHandleJobClick(menu, jobIds, tabTitle) {
        if(!menu || !jobIds || jobIds.length === 0) {
            return;
        }

        var jobIdsParam = jobIds.join(',');
        var baseUrl = fnResolveMenuUrl(menu);
        var separator = baseUrl.indexOf('?') > -1 ? '&' : '?';

        fnHandleMenuClick({
            menuId: menu.menuId,
            menuNm: tabTitle,
            menuUrl: baseUrl + separator + 'jobIds=' + encodeURIComponent(jobIdsParam),
            openType: menu.openType,
            jobId: jobIdsParam
        });
    }

    /**
     * 브레드크럼(Home > 상위메뉴 > ... > 현재메뉴)을 렌더링한다.
     * 마지막 항목(현재 메뉴)은 링크 없이 active 표시만 하고,
     * 그 앞 항목들은 클릭 가능하도록 data-menu-id를 부여한다.
     * @param currentChain 최상위부터 현재 메뉴까지의 체인 배열
     */
    function fnRenderBreadcrumb(currentChain) {
        var html = '<li class="breadcrumb-item home"><a href="/main.do">Home</a></li>';

        currentChain.forEach(function(menu, idx) {
            var isLast = (idx === currentChain.length - 1);
            if(isLast) {
                html += '<li class="breadcrumb-item active">' + menu.menuNm + '</li>';
            } else {
                html += '<li class="breadcrumb-item" data-menu-id="' + menu.menuId + '">' + menu.menuNm + '</li>';
            }
        });

        _$breadcrumb.html(html);
        fnBindBreadcrumbEvents();
    }

    /**
     * 현재 메뉴명을 페이지 타이틀 영역에 표시한다. 현재 메뉴가 없으면 빈 문자열로 초기화.
     * @param currentMenu 현재 선택된 메뉴 객체
     */
    function fnRenderPageTitle(currentMenu) {
        _$pageTitle.text(currentMenu ? currentMenu.menuNm : '');
    }

    /**
     * 현재 페이지 경로를 반환한다. 메뉴 매칭 비교 기준으로 사용됨.
     * @returns location.pathname
     */
    function fnGetCurrentPath() {
        return location.pathname;
    }

    /**
     * 메뉴 클릭(또는 외부 호출) 시 openType(본창/새창/탭/팝업/모달)에 따라 실제 이동 방식을 분기한다.
     * - WINDOW: 새 브라우저 창으로 오픈
     * - TAB: ComTab을 통해 현재 화면 내 탭(iframe)으로 오픈
     * - POPUP: 크기 지정된 팝업 창으로 오픈
     * - MODAL: ComMsg 모달로 오픈
     * - 기본(본창): location.href로 페이지 전체 이동 (열려있던 탭은 전부 초기화됨)
     * @param menu 이동 대상 메뉴 객체
     */
    function fnHandleMenuClick(menu) {
        var url = fnResolveMenuUrl(menu);

        switch(menu.openType) {
            case 'WINDOW':
                window.open(url, '_blank');
                break;
            case 'TAB':
                ComTab.openTab(menu.menuId, menu.jobId || null, menu.menuNm, url);
                break;
            case 'POPUP':
                window.open(url, 'popup_' + menu.menuId,
                    'width=1000,height=800,scrollbars=yes,resizable=yes');
                break;
            case 'MODAL':
                ComMsg.modalUrl(url);
                break;
            default:
                // 본창: 페이지 전체 이동, 열려있던 탭은 전부 초기화됨
                location.href = url;
        }
    }

    /**
     * GNB 메뉴 항목 클릭 이벤트를 바인딩한다.
     * 재렌더링마다 중복 바인딩되지 않도록 off 후 on 처리.
     */
    function fnBindNavEvents() {
        _$navContainer.off('click', '.menu-item').on('click', '.menu-item', function() {
            var menuId = $(this).data('menu-id');
            var menu = fnFindMenuById(menuId);
            if(menu) {
                fnHandleMenuClick(menu);
            }
        });
    }

    /**
     * LNB 메뉴 항목 클릭 이벤트를 바인딩한다.
     * 재렌더링마다 중복 바인딩되지 않도록 off 후 on 처리.
     *
     * job-list가 lnb-item div 안에 중첩되지 않고 형제 div로 분리되어 있으므로,
     * 버블링으로 인한 이벤트 충돌 걱정 없이 각각 독립적으로 바인딩한다.
     */
    function fnBindLnbEvents() {
        _$lnbContainer.off('click', '.lnb-item').on('click', '.lnb-item', function() {
            var menuId = $(this).data('menu-id');
            var menu = fnFindMenuById(menuId);
            if(menu) {
                fnHandleMenuClick(menu);
            }
        });

        // '선택된job열기' 버튼은 job-list-wrap에 하나만 존재한다.
        // 체크된 checkbox들의 jobId를 모아서 탭 하나에 한번에 연다 (job마다 탭을 따로 열지 않음).
        _$lnbContainer.off('click', '.job-open-btn').on('click', '.job-open-btn', function() {
            var $wrap = $(this).closest('.job-list-wrap');
            var $itemDiv = $wrap.prev('.lnb-item');
            var menuId = $itemDiv.data('menu-id');
            var menu = fnFindMenuById(menuId);

            if(!menu) {
                return;
            }

            var checkedJobIds = [];
            var checkedJobNms = [];

            $wrap.find('.job-checkbox:checked').each(function() {
                checkedJobIds.push($(this).data('job-id'));
                checkedJobNms.push($(this).closest('li').find('label').text());
            });

            if(checkedJobIds.length === 0) {
                ComMsg.alert('열 job을 선택해주세요.');
                return;
            }

            var tabTitle = checkedJobNms.join('+');
            fnHandleJobClick(menu, checkedJobIds, tabTitle);
        });
    }

    /**
     * 브레드크럼 항목(현재 메뉴 제외) 클릭 이벤트를 바인딩한다.
     * data-menu-id가 있는 항목(=마지막 active 항목 제외)만 대상으로 한다.
     */
    function fnBindBreadcrumbEvents() {
        _$breadcrumb.off('click', '.breadcrumb-item[data-menu-id]').on('click', '.breadcrumb-item[data-menu-id]', function() {
            var menuId = $(this).data('menu-id');
            var menu = fnFindMenuById(menuId);
            if(menu) {
                fnHandleMenuClick(menu);
            }
        });
    }

    /**
     * 외부(다른 화면/스크립트)에서 menuId 기준으로 메뉴 이동을 호출할 수 있게 하는 공개 API.
     * 존재하지 않는 menuId면 콘솔 경고만 남기고 아무 동작도 하지 않는다.
     * 예: 대시보드 위젯의 "바로가기" 버튼 -> ComMenu.moveToMenu('MENU_0012')
     * @param menuId 이동할 메뉴 ID
     */
    function fnMoveToMenu(menuId) {
        var menu = fnFindMenuById(menuId);
        if(!menu) {
            console.warn('[ComMenu] 존재하지 않는 menuId: ' + menuId);
            return;
        }
        fnHandleMenuClick(menu);
    }

    /**
     * 외부에서 menuId 없이 임의의 URL로 바로 이동시키고 싶을 때 사용하는 공개 API.
     * openType을 별도로 지정하지 않으면 기본값(본창 이동)으로 동작한다.
     * 예: ComMenu.moveToUrl('/doc/detail.do?id=123', 'TAB')
     * @param url 이동할 URL
     * @param openType 'WINDOW' | 'TAB' | 'POPUP' | 'MODAL' 중 하나, 생략 시 본창 이동
     */
    function fnMoveToUrl(url, openType) {
        fnHandleMenuClick({
            menuId: null,
            menuNm: '',
            menuUrl: url,
            openType: openType
        });
    }

    // 외부에는 init, moveToMenu, moveToUrl 노출 (모듈 패턴)
    return {
        init: fnInit,
        moveToMenu: fnMoveToMenu,
        moveToUrl: fnMoveToUrl
    };
})();