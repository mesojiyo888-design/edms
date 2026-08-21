var ComTab = (function() {
    'use strict';

    var MAX_TAB_COUNT = 10;   // 탭 최대 개수 제한

    var _tabs = [];
    var _$tabWrapper = null;   // tab-bar/tab-content 공통 부모, [data-nds="tabs"] 컨테이너로 추정 (initTabs 대상)
    var _$tabBar = null;       // #tab-bar 안의 ul
    var _$tabContent = null;   // #tab-content
    var _tabBarSelector = null;
    var _tabContentSelector = null;
    var _activeTabId = null;
    var _tabSeq = 0;

    /**
     * ComTab 초기화. tab-bar와 tab-content는 같은 부모 노드의 형제 엘리먼트여야 하며,
     * 그 공통 부모가 initTabs가 스캔하는 [data-nds="tabs"] 래퍼가 된다.
     * @param tabBarSelector #tab-bar 셀렉터 (내부에 ul이 있는 div)
     * @param tabContentSelector #tab-content 셀렉터
     */
    function fnInit(tabBarSelector, tabContentSelector) {
        _tabBarSelector = tabBarSelector;
        _tabContentSelector = tabContentSelector;

        _$tabBar = $(tabBarSelector);
        _$tabContent = $(tabContentSelector);
        _$tabWrapper = _$tabBar.parent();   // tab-bar/tab-content 공통 부모를 래퍼로 사용

        fnRegisterExistingTabs();
    }

    /**
     * 마크업에 이미 박혀있는 탭(고정 탭 + 동적 생성 탭 재스캔 포함)을 _tabs 배열에 등록한다.
     * remove 버튼이 없는 탭은 closable: false로 등록하여 닫기를 원천 차단한다.
     * _tabSeq도 기존 tabId 숫자 중 최댓값에 맞춰 이어지게 보정한다.
     *
     * fnOpenTab이 생성하는 <li>도 data-menu-id/data-job-id를 동일하게 갖고 있어야
     * fnInit이 재호출되는 상황에서도 menuId/jobId를 정확히 복원할 수 있다.
     */
    function fnRegisterExistingTabs() {
        _tabs = [];
        _tabSeq = 0;

        _$tabBar.find('li[role="tab"]').each(function() {
            var $tabEl = $(this);
            var tabId = $tabEl.attr('id');
            var panelId = $tabEl.attr('aria-controls');
            var closable = ($tabEl.find('.n-tab-remove').length > 0);
            var menuId = $tabEl.data('menuId') || tabId;
            var jobIdRaw = $tabEl.data('jobId');
            var jobId = (jobIdRaw === '' || jobIdRaw == null) ? null : jobIdRaw;

            _tabs.push({
                tabId: tabId,
                panelId: panelId,
                menuId: menuId,
                jobId: jobId,
                tabKey: fnBuildTabKey(menuId, jobId),
                title: $tabEl.find('.tab-title-btn').text() || $tabEl.text(),
                url: null,
                closable: closable
            });

            var seqNum = parseInt(tabId.replace(/^t/, ''), 10);
            if(!isNaN(seqNum) && seqNum > _tabSeq) {
                _tabSeq = seqNum;
            }

            if($tabEl.attr('aria-selected') === 'true') {
                _activeTabId = tabId;
            }
        });

        _$tabBar.find('.n-tab-remove').off('click').on('click', function(e) {
            e.stopPropagation();
            var tabId = $(this).closest('li[role="tab"]').attr('id');
            fnCloseTab(tabId);
        });
    }

    /**
     * 탭을 연다. 이미 열려있는 menuId+jobId 조합이면 재로드 없이 해당 탭으로 전환만 한다.
     * jobId가 없는 일반 메뉴는 menuId만으로 매칭한다(기존 동작 그대로).
     * 최대 개수(MAX_TAB_COUNT) 초과 시 신규 생성은 막는다.
     * @param menuId 메뉴 ID
     * @param jobId job ID (job 목록에서 연 탭이 아니면 null/undefined)
     * @param title 탭 제목
     * @param url 이동할 URL
     */
    function fnOpenTab(menuId, jobId, title, url) {
        var tabKey = fnBuildTabKey(menuId, jobId);
        var existingTab = fnFindTabByKey(tabKey);

        if(existingTab) {
            fnSelectByTabId(existingTab.tabId);
            return;
        }

        if(_tabs.length >= MAX_TAB_COUNT) {
            ComMsg.alert('탭은 최대 ' + MAX_TAB_COUNT + '개까지 열 수 있습니다. 사용하지 않는 탭을 닫아주세요.');
            return;
        }

        _tabSeq++;
        var tabId = 't' + _tabSeq;
        var panelId = 'p' + _tabSeq;
        var iframeUrl = fnAppendTabModeParam(url);

        var $tabItem = $(
            '<li role="tab" id="' + tabId + '" aria-selected="false" aria-controls="' + panelId + '"' +
                ' data-menu-id="' + menuId + '" data-job-id="' + (jobId || '') + '">' +
                '<button type="button" class="tab-title-btn" title="' + title + '">' + title + '</button>' +
                '<button type="button" class="n-tab-remove" aria-label="삭제"></button>' +
            '</li>'
        );

        var $tabPanel = $(
            '<div role="tabpanel" id="' + panelId + '" aria-labelledby="' + tabId + '" class="tab-panel" hidden>' +
                '<iframe class="tab-iframe" src="' + iframeUrl + '"></iframe>' +
            '</div>'
        );

        $tabItem.find('.n-tab-remove').on('click', function(e) {
            e.stopPropagation();
            fnCloseTab(tabId);
        });

        _$tabBar.append($tabItem);
        _$tabContent.append($tabPanel);

        _tabs.push({
            tabId: tabId,
            panelId: panelId,
            menuId: menuId,
            jobId: jobId || null,
            tabKey: tabKey,
            title: title,
            url: url,
            closable: true
        });

        fnReinitNdsTabs();
        fnSelectByTabId(tabId);
    }

    function fnBuildTabKey(menuId, jobId) {
        return jobId ? (menuId + '_' + jobId) : menuId;
    }

    function fnAppendTabModeParam(url) {
        var separator = url.indexOf('?') > -1 ? '&' : '?';
        return url + separator + '_tabMode=Y';
    }

    function fnFindTabByKey(tabKey) {
        for(var i = 0; i < _tabs.length; i++) {
            if(_tabs[i].tabKey === tabKey) {
                return _tabs[i];
            }
        }
        return null;
    }

    function fnFindTabById(tabId) {
        for(var i = 0; i < _tabs.length; i++) {
            if(_tabs[i].tabId === tabId) {
                return _tabs[i];
            }
        }
        return null;
    }

    function fnSelectByTabId(tabId) {
        var $tabEl = _$tabBar.find('#' + tabId);
        if($tabEl.length === 0) {
            return;
        }
        _activeTabId = tabId;
        $tabEl.trigger('click');
    }

    function fnCloseTab(tabId) {
        var closingIdx = _tabs.findIndex(function(tab) {
            return tab.tabId === tabId;
        });

        if(closingIdx === -1) {
            return;
        }

        var closingTab = _tabs[closingIdx];

        if(closingTab.closable === false) {
            return;
        }

        var wasActive = (_activeTabId === tabId);

        _$tabBar.find('#' + closingTab.tabId).remove();
        _$tabContent.find('#' + closingTab.panelId).remove();
        _tabs.splice(closingIdx, 1);

        fnReinitNdsTabs();

        if(wasActive) {
            var nextTab = _tabs[closingIdx] || _tabs[closingIdx - 1];
            if(nextTab) {
                fnSelectByTabId(nextTab.tabId);
            } else {
                _activeTabId = null;
            }
        }
    }

    /**
     * 탭 DOM이 추가/삭제된 뒤 재초기화한다.
     * clone(false)로 리스너를 제거한 뒤 재바인딩하고,
     * _$tabBar/_$tabContent는 detached 참조 문제를 피하기 위해
     * 저장해둔 원래 셀렉터로 다시 조회한다 (래퍼를 거치지 않음).
     * TODO: initTabs 소스 확인되면 이 clone 방어 로직은 제거 가능.
     */
    function fnReinitNdsTabs() {
        if(typeof initTabs !== 'function') {
            return;
        }

        _$tabBar.find('li[role="tab"]').each(function() {
            var $clone = $(this).clone(false);
            $(this).replaceWith($clone);

            var tabId = $clone.attr('id');
            var tabInfo = fnFindTabById(tabId);

            if(tabInfo && tabInfo.closable !== false) {
                $clone.find('.n-tab-remove').on('click', function(e) {
                    e.stopPropagation();
                    fnCloseTab(tabId);
                });
            }
        });

        _$tabBar = $(_tabBarSelector);
        _$tabContent = $(_tabContentSelector);

        _$tabWrapper.each(function() {
            initTabs(this);
        });
    }

    return {
        init: fnInit,
        openTab: fnOpenTab,
        closeTab: fnCloseTab
    };
})();