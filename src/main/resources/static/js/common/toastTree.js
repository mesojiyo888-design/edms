// ===================================================================
// ComTree.js
// TOAST UI Tree v4.0.9 공통 래퍼 (컨텍스트메뉴 + 롤백 지원)
//
// tui-tree 4.0.9 실제 API 기준:
//   add(nodeData, parentId, index)  → nodeId 반환
//   remove(nodeId)                  → 노드+하위 전부 삭제
//   setNodeData(nodeId, data)       → 데이터 병합 수정
//   getNodeData(nodeId)             → 데이터 조회
//   getParentId(nodeId)             → 부모 nodeId 조회 (최상위면 rootNodeId)
//   getNodeIndex(nodeId)            → 부모 하위에서의 순서(index) 조회
//   getNodeIdFromElement(element)   → DOM 엘리먼트 → nodeId 역추적
//   isLeaf(nodeId)                  → 자식 없는 노드인지 여부
//   getRootNodeId()                 → 최상위 루트 nodeId
//
// 동작 흐름:
//   1) 노드 우클릭 → 컨텍스트메뉴 표시 (하위추가 / 이름수정 / 삭제)
//   2) 메뉴 클릭 → prompt/confirm → 트리에 즉시 반영 (add/setNodeData/remove)
//   3) 트리 반영 완료 직후 → register 시 넘긴 onAddChild/onEdit/onDelete 콜백 호출
//      콜백 인자: (nodeId, data, rollback)
//   4) 화면(business)이 콜백 안에서 DB 저장 ajax를 직접 작성
//      → 실패하면 콜백으로 받은 rollback() 을 호출해서 트리를 원상복구
//
// 서버통신(ajax)은 이 파일에 없음. 전부 화면 콜백에서 작성한다.
// ===================================================================
var ComTree = (function()
{
    var _trees      = {};   // treeId → tui.Tree 인스턴스
    var _idMap       = {};  // treeId → { customId: internalNodeId }
    var _handlersMap = {};  // treeId → { onAddChild, onEdit, onDelete } (register 시 등록)
    var _maxDepthMap = {};  // treeId → maxDepth (register 시 등록, 없으면 제한 없음)
    var _optionsMap  = {};  // treeId → register 시 넘긴 options 원본 (reload 시 fieldMap/rootLabel 재사용)
    var $activeMenu  = null;

    // -----------------------------------------------------------------
    // 트리 등록 (최초 렌더링 + 컨텍스트메뉴 바인딩)
    //
    // 사용법 :
    //   ComTree.register('docFolderTree', 'docFolderTree',
    //   {
    //       data      : folderData,
    //       onSelect  : function(nodeId, nodeData) { ... },
    //       contextMenu:
    //       {
    //           onAddChild: function(nodeId, nodeData, rollback) { ... 화면에서 DB저장 ajax ... },
    //           onEdit    : function(nodeId, nodeData, rollback) { ... },
    //           onDelete  : function(nodeId, nodeData, rollback) { ... }
    //       }
    //   });
    // -----------------------------------------------------------------
    /**
     * container로 넘어온 값을 실제 DOM 엘리먼트로 변환
     * - HTMLElement를 직접 넘기면 그대로 사용
     * - 문자열이면 '#' 유무 상관없이 id로 간주해서 document.getElementById 로 조회
     */
    function _resolveElement(container)
    {
        if(container instanceof HTMLElement)
        {
            return container;
        }

        if(typeof container === 'string')
        {
            var id = (container.charAt(0) === '#') ? container.substring(1) : container;
            return document.getElementById(id);
        }

        return null;
    }

    // -----------------------------------------------------------------
    // 트리 등록 (최초 렌더링 + 컨텍스트메뉴 바인딩)
    //
    // 사용법 1) 이미 중첩된 tree 구조 데이터를 직접 넘기는 경우 :
    //   ComTree.register('menuTree', 'menuTree',
    //   {
    //       data      : nestedTreeData,
    //       onSelect  : function(nodeId, nodeData) { ... },
    //       contextMenu: { onAddChild, onEdit, onDelete }
    //   });
    //
    // 사용법 2) 오라클에서 조회한 평면(flat) 리스트를 그대로 넘기는 경우 :
    //   ComTree.register('menuTree', 'menuTree',
    //   {
    //       flatData: flatList,                  // [{ menuId, upperMenuId, menuNm, menuOrder }, ...]
    //       fieldMap:
    //       {
    //           id      : 'menuId',
    //           parentId: 'upperMenuId',
    //           text    : 'menuNm',
    //           order   : 'menuOrder'            // 생략 가능 (정렬 안 함)
    //       },
    //       rootParentId: null,                  // 최상위 노드의 parentId 값 (기본 null)
    //       rootLabel   : '전체 메뉴',            // (선택) 최상위에 루트 노드를 하나 더 씌우고 싶을 때 이름 지정
    //       rootId      : 'ROOT',                 // (선택) 루트 노드의 customId, 생략 시 '__ROOT__'
    //       maxDepth    : 3,                      // (선택) 하위 추가 가능한 최대 depth (생략 시 제한 없음)
    //       contextMenu : { onAddChild, onEdit, onDelete }
    //   });
    //   → flatData가 있으면 fieldMap 기준으로 내부에서 자동 변환 후 렌더링합니다.
    //   → rootLabel을 주면 변환된(또는 넘겨받은) 트리 전체를 그 이름의 루트 노드 하나로 감쌉니다.
    // -----------------------------------------------------------------
    function register(treeId, container, options)
    {
        if(_trees[treeId])
        {
            _trees[treeId].destroy();
        }

        options = options || {};

        var containerEl = _resolveElement(container);

        if(!containerEl)
        {
            console.error('ComTree: container 엘리먼트를 찾을 수 없습니다 → ' + container);
            return null;
        }

        // tui-tree 자체 스타일 적용을 위한 필수 클래스 (없으면 자동 추가)
        if(containerEl.className.indexOf('tui-tree-wrap') === -1)
        {
            containerEl.className += ' tui-tree-wrap';
        }

        // flatData(평면 리스트)가 넘어오면 fieldMap 기준으로 내부에서 자동 변환, 아니면 options.data 그대로 사용
        var nestedData = options.data || [];

        if(options.flatData)
        {
            nestedData = convertFlatToTree(options.flatData, options.fieldMap, options.rootParentId);
        }

        // rootLabel이 있으면 전체를 감싸는 최상위 루트 노드 하나를 추가로 씌운다
        if(options.rootLabel)
        {
            nestedData =
            [
                {
                    text    : options.rootLabel,
                    id      : options.rootId || '__ROOT__',
                    children: nestedData
                }
            ];
        }

        var tree = new tui.Tree(containerEl,
        {
            data            : nestedData,
            nodeDefaultState: options.defaultState || 'opened'
        });

        // tui-tree v4는 기능들이 기본 비활성 상태라 명시적으로 켜야 함
        tree.enableFeature('Selectable', { selectedClassName: 'tui-tree-selected' });

        if(options.checkbox)
        {
            tree.enableFeature('Checkbox');
        }

        // 'select' 이벤트는 nodeId 문자열이 아니라 {nodeId, prevNodeId, target} 객체를 넘겨준다
        tree.on('select', function(eventData)
        {
            var selectedNodeId = eventData && eventData.nodeId;

            if(typeof options.onSelect === 'function' && selectedNodeId)
            {
                options.onSelect(selectedNodeId, tree.getNodeData(selectedNodeId));
            }
        });

        _trees[treeId] = tree;
        _idMap[treeId] = {};
        _maxDepthMap[treeId] = options.maxDepth;   // 없으면 undefined → 제한 없음
        _optionsMap[treeId]  = options;            // reload 시 fieldMap/rootLabel 등 재사용

        // 우리가 직접 만든 커스텀 컨텍스트메뉴를 쓸 것이므로 tui-tree 자체
        // ContextMenu 기능(tui-context-menu 라이브러리 필요)은 사용하지 않는다.
        tree.disableFeature && tree.disableFeature('ContextMenu');

        _rebuildIdMap(treeId);

        if(options.contextMenu)
        {
            _handlersMap[treeId] = options.contextMenu;
            _bindContextMenu(treeId, containerEl);
        }

        return tree;
    }

    /**
     * 트리 전체 노드를 순회해 customId ↔ nodeId 매핑을 (재)구축
     * search({}) 로는 전체 노드를 가져오기 애매하므로, 재귀적으로 getChildIds를 순회한다.
     */
    function _rebuildIdMap(treeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return;
        }

        _idMap[treeId] = {};

        var rootId = tree.getRootNodeId();

        (function walk(nodeId)
        {
            var childIds = tree.getChildIds(nodeId) || [];

            childIds.forEach(function(childId)
            {
                var data = tree.getNodeData(childId);

                if(data && data.id)
                {
                    _idMap[treeId][data.id] = childId;
                }

                walk(childId);
            });
        })(rootId);
    }

    function getTree(treeId)
    {
        if(!_trees[treeId])
        {
            console.warn('ComTree: 등록되지 않은 treeId 입니다 → ' + treeId);
            return null;
        }

        return _trees[treeId];
    }

    // -----------------------------------------------------------------
    // 컨텍스트메뉴 바인딩 (커스텀 - tui-context-menu 라이브러리 불필요)
    // -----------------------------------------------------------------
    function _bindContextMenu(treeId, container)
    {
        var tree = getTree(treeId);
        var $container = $(container);

        $container.on('contextmenu', function(e)
        {
            e.preventDefault();

            var nodeId = tree.getNodeIdFromElement(e.target);

            if(!nodeId)
            {
                return;
            }

            tree.select(nodeId);
            _showContextMenu(treeId, nodeId, e.pageX, e.pageY);
        });

        $(document).on('click', function()
        {
            _closeContextMenu();
        });
    }

    function _closeContextMenu()
    {
        if($activeMenu)
        {
            $activeMenu.remove();
            $activeMenu = null;
        }
    }

    function _showContextMenu(treeId, nodeId, x, y)
    {
        _closeContextMenu();

        var handlers = _handlersMap[treeId] || {};
        var $menu    = $('<ul class="com-tree-context-menu"></ul>').css(
        {
            position  : 'absolute',
            top       : y + 'px',
            left      : x + 'px',
            zIndex    : 9999,
            listStyle : 'none',
            margin    : 0,
            padding   : '4px 0',
            background: '#fff',
            border    : '1px solid #ccc',
            boxShadow : '0 2px 6px rgba(0,0,0,0.15)',
            minWidth  : '120px'
        });

        function addMenuItem(label, onClick)
        {
            $('<li></li>')
                .text(label)
                .css({ padding: '6px 14px', cursor: 'pointer', fontSize: '13px' })
                .hover(
                    function() { $(this).css('background', '#f0f0f0'); },
                    function() { $(this).css('background', '#fff'); }
                )
                .on('click', function(e)
                {
                    e.stopPropagation();
                    _closeContextMenu();
                    onClick();
                })
                .appendTo($menu);
        }

        if(handlers.onAddChild)
        {
            addMenuItem('하위 폴더 추가', function() { _handleAddChild(treeId, nodeId, handlers); });
        }

        if(handlers.onEdit)
        {
            addMenuItem('이름 수정', function() { _handleEdit(treeId, nodeId, handlers); });
        }

        if(handlers.onDelete)
        {
            addMenuItem('삭제', function() { _handleDelete(treeId, nodeId, handlers); });
        }

        $menu.on('click', function(e) { e.stopPropagation(); });

        $('body').append($menu);
        $activeMenu = $menu;
    }

    // -----------------------------------------------------------------
    // 이름 입력용 커스텀 모달 (prompt() 대체)
    //
    // 사용법 : _showInputModal({ title: '폴더 등록', label: '폴더명', value: '', onConfirm: function(text) {...} })
    // -----------------------------------------------------------------
    var $activeModal = null;

    function _closeInputModal()
    {
        if($activeModal)
        {
            $activeModal.remove();
            $activeModal = null;
        }

        $(document).off('keydown.comTreeModal');
    }

    function _showInputModal(options)
    {
        _closeInputModal();

        var $overlay = $('<div class="com-tree-modal-overlay"></div>').css(
        {
            position  : 'fixed',
            top       : 0,
            left      : 0,
            right     : 0,
            bottom    : 0,
            background: 'rgba(0,0,0,0.35)',
            zIndex    : 10000,
            display   : 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        });

        var $box = $('<div class="com-tree-modal-box"></div>').css(
        {
            background  : '#fff',
            borderRadius: '6px',
            padding     : '20px 24px',
            minWidth    : '280px',
            boxShadow   : '0 4px 16px rgba(0,0,0,0.25)',
            fontSize    : '14px'
        });

        var $title = $('<div></div>')
            .text(options.title || '입력')
            .css({ fontSize: '15px', fontWeight: 'bold', marginBottom: '12px' });

        var $label = $('<label></label>')
            .text(options.label || '')
            .css({ display: 'block', marginBottom: '6px', color: '#555' });

        var $input = $('<input type="text">')
            .val(options.value || '')
            .css(
            {
                width       : '100%',
                boxSizing   : 'border-box',
                padding     : '8px 10px',
                border      : '1px solid #ccc',
                borderRadius: '4px',
                fontSize    : '14px',
                marginBottom: '4px'
            });

        var $hint = $('<div></div>')
            .text('영어, 숫자, 한글, -, _ 만 입력할 수 있습니다.')
            .css({ fontSize: '11px', color: '#999', marginBottom: '12px' });

        var $btnWrap = $('<div></div>').css({ textAlign: 'right' });

        var $cancelBtn = $('<button type="button">취소</button>').css(
        {
            padding     : '6px 14px',
            marginRight : '6px',
            border      : '1px solid #ccc',
            borderRadius: '4px',
            background  : '#fff',
            cursor      : 'pointer'
        });

        var $confirmBtn = $('<button type="button">확인</button>').css(
        {
            padding     : '6px 14px',
            border      : 'none',
            borderRadius: '4px',
            background  : '#3182f6',
            color       : '#fff',
            cursor      : 'pointer'
        });

        // 허용 문자 : 영어, 숫자, 한글, -, _ 만
        var NAME_PATTERN = /^[a-zA-Z0-9_\-가-힣]*$/;

        // 입력하는 즉시 허용 안 된 문자는 걸러낸다 (붙여넣기 포함)
        $input.on('input', function()
        {
            var cur = $input.val();

            if(!NAME_PATTERN.test(cur))
            {
                $input.val(cur.replace(/[^a-zA-Z0-9_\-가-힣]/g, ''));
                $input.css('border-color', '#e03131');
            }
            else
            {
                $input.css('border-color', '#ccc');
            }
        });

        function submit()
        {
            var value = $input.val();

            if(!value || value.trim() === '')
            {
                $input.css('border-color', '#e03131').focus();
                return;
            }

            if(!NAME_PATTERN.test(value.trim()))
            {
                $input.css('border-color', '#e03131').focus();
                return;
            }

            _closeInputModal();

            if(typeof options.onConfirm === 'function')
            {
                options.onConfirm(value.trim());
            }
        }

        $confirmBtn.on('click', submit);
        $cancelBtn.on('click', _closeInputModal);
        $overlay.on('click', function(e) { if(e.target === $overlay[0]) { _closeInputModal(); } });

        $(document).on('keydown.comTreeModal', function(e)
        {
            if(e.key === 'Enter')
            {
                submit();
            }
            else if(e.key === 'Escape')
            {
                _closeInputModal();
            }
        });

        $btnWrap.append($cancelBtn, $confirmBtn);
        $box.append($title, $label, $input, $hint, $btnWrap);
        $overlay.append($box);
        $('body').append($overlay);

        $activeModal = $overlay;

        setTimeout(function() { $input.focus().select(); }, 0);
    }

    // -----------------------------------------------------------------
    // 컨텍스트메뉴 - 하위 폴더 추가
    // -----------------------------------------------------------------
    function _handleAddChild(treeId, parentNodeId, handlers)
    {
        var maxDepth = _maxDepthMap[treeId];

        if(maxDepth !== undefined && maxDepth !== null)
        {
            var tree         = getTree(treeId);
            var parentDepth  = tree.getDepth(parentNodeId);   // 루트 depth 기준값은 tui-tree 내부 정의에 따름
            var newNodeDepth = parentDepth + 1;

            if(newNodeDepth > maxDepth)
            {
                _alertFallback('최대 ' + maxDepth + '단계까지만 하위 항목을 추가할 수 있습니다.');
                return;
            }
        }

        _showInputModal(
        {
            title: '하위 항목 추가',
            label: '이름을 입력하세요',
            value: '',
            onConfirm: function(folderName)
            {
                addNode(treeId, parentNodeId, { text: folderName }, undefined, function(newNodeId, nodeData, rollback)
                {
                    if(typeof handlers.onAddChild === 'function')
                    {
                        handlers.onAddChild(newNodeId, nodeData, rollback);
                    }
                });
            }
        });
    }

    // -----------------------------------------------------------------
    // 컨텍스트메뉴 - 이름 수정
    // -----------------------------------------------------------------
    function _handleEdit(treeId, nodeId, handlers)
    {
        var tree    = getTree(treeId);
        var current = tree.getNodeData(nodeId);

        _showInputModal(
        {
            title: '이름 수정',
            label: '새 이름을 입력하세요',
            value: current.text,
            onConfirm: function(newName)
            {
                if(newName === current.text)
                {
                    return;
                }

                editNodeData(treeId, nodeId, { text: newName }, function(nodeId, mergedData, rollback)
                {
                    if(typeof handlers.onEdit === 'function')
                    {
                        handlers.onEdit(nodeId, mergedData, rollback);
                    }
                });
            }
        });
    }

    // -----------------------------------------------------------------
    // 컨텍스트메뉴 - 삭제
    // -----------------------------------------------------------------
    function _handleDelete(treeId, nodeId, handlers)
    {
        if(!isLeaf(treeId, nodeId))
        {
            _alertFallback('하위 폴더가 있는 항목은 삭제할 수 없습니다.');
            return;
        }

        _confirmFallback('선택한 폴더를 삭제하시겠습니까?', function()
        {
            removeNode(treeId, nodeId, function(nodeId, removedData, rollback)
            {
                if(typeof handlers.onDelete === 'function')
                {
                    handlers.onDelete(nodeId, removedData, rollback);
                }
            });
        });
    }

    /**
     * 알림 표시 - 브라우저 기본 alert 사용 (ComMsg 등 외부 의존성 없음)
     */
    function _alertFallback(message)
    {
        alert(message);
    }

    /**
     * 확인창 표시 - 브라우저 기본 confirm 사용 (ComMsg 등 외부 의존성 없음)
     * confirm()은 콜백이 아니라 true/false를 리턴하는 동기 함수라서,
     * 여기서 리턴값을 받아 onConfirm 콜백 스타일로 통일해준다.
     */
    function _confirmFallback(message, onConfirm)
    {
        if(confirm(message))
        {
            onConfirm();
        }
    }

    // -----------------------------------------------------------------
    // 노드 추가 - callback(nodeId, nodeData, rollback)
    // rollback() 호출 시 방금 추가한 노드를 다시 삭제해서 원상복구
    //
    // 사용법 : ComTree.addNode('docFolderTree', parentNodeId, { text: '새 폴더' }, undefined,
    //          function(nodeId, data, rollback) { ... })
    // -----------------------------------------------------------------
    function addNode(treeId, parentNodeId, nodeData, index, callback)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return null;
        }

        var newNodeId = tree.add(nodeData, parentNodeId, index);

        if(typeof tree.refresh === 'function')
        {
            tree.refresh();
        }

        if(nodeData.id)
        {
            _idMap[treeId][nodeData.id] = newNodeId;
        }

        var rollback = function()
        {
            tree.remove(newNodeId);

            if(typeof tree.refresh === 'function')
            {
                tree.refresh();
            }

            if(nodeData.id)
            {
                delete _idMap[treeId][nodeData.id];
            }
        };

        if(typeof callback === 'function')
        {
            callback(newNodeId, nodeData, rollback);
        }

        return newNodeId;
    }

    /**
     * 노드 텍스트만 수정
     * 사용법 : ComTree.editNodeText('docFolderTree', nodeId, '보관 문서', function(nodeId, data, rollback) {...})
     */
    function editNodeText(treeId, nodeId, newText, callback)
    {
        return editNodeData(treeId, nodeId, { text: newText }, callback);
    }

    // -----------------------------------------------------------------
    // 노드 데이터 수정 - callback(nodeId, mergedData, rollback)
    // rollback() 호출 시 수정 전 데이터로 되돌림
    //
    // 사용법 : ComTree.editNodeData('docFolderTree', nodeId, { text: '보관 문서' },
    //          function(nodeId, data, rollback) { ... })
    // -----------------------------------------------------------------
    function editNodeData(treeId, nodeId, newData, callback)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        var oldData = tree.getNodeData(nodeId);

        if(!oldData)
        {
            console.warn('ComTree: 존재하지 않는 노드입니다 → ' + nodeId);
            return false;
        }

        // 롤백용으로 수정 전 데이터를 얕은 복사해 둔다
        var backup = $.extend({}, oldData);

        tree.setNodeData(nodeId, newData);

        if(typeof tree.refresh === 'function')
        {
            tree.refresh();
        }

        var merged = tree.getNodeData(nodeId);

        var rollback = function()
        {
            tree.setNodeData(nodeId, backup);

            if(typeof tree.refresh === 'function')
            {
                tree.refresh();
            }
        };

        if(typeof callback === 'function')
        {
            callback(nodeId, merged, rollback);
        }

        return true;
    }

    // -----------------------------------------------------------------
    // 노드 삭제 - callback(nodeId, removedData, rollback)
    // rollback() 호출 시 삭제했던 노드를 원래 부모의 원래 위치(index)에 다시 추가
    //
    // 사용법 : ComTree.removeNode('docFolderTree', nodeId, function(nodeId, data, rollback) { ... })
    // -----------------------------------------------------------------
    function removeNode(treeId, nodeId, callback)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        var nodeData = tree.getNodeData(nodeId);

        if(!nodeData)
        {
            console.warn('ComTree: 존재하지 않는 노드입니다 → ' + nodeId);
            return false;
        }

        // 삭제 전에 복원용 정보(부모 id, 원래 순서) 미리 확보
        var parentId  = tree.getParentId(nodeId);
        var nodeIndex = tree.getNodeIndex(nodeId);
        var backup    = $.extend({}, nodeData);

        tree.remove(nodeId);

        if(typeof tree.refresh === 'function')
        {
            tree.refresh();
        }

        if(nodeData.id)
        {
            delete _idMap[treeId][nodeData.id];
        }

        var rollback = function()
        {
            var restoredNodeId = tree.add(backup, parentId, nodeIndex);

            if(typeof tree.refresh === 'function')
            {
                tree.refresh();
            }

            if(backup.id)
            {
                _idMap[treeId][backup.id] = restoredNodeId;
            }
        };

        if(typeof callback === 'function')
        {
            callback(nodeId, nodeData, rollback);
        }

        return true;
    }

    function moveNode(treeId, nodeId, newParentId, index)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        tree.move(nodeId, newParentId, index);

        return true;
    }

    /**
     * 노드가 리프(자식 없음)인지 확인 - 삭제 전 유효성 체크용
     * 사용법 : ComTree.isLeaf('docFolderTree', nodeId)
     */
    function isLeaf(treeId, nodeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        return tree.isLeaf(nodeId);
    }

    /**
     * 트리 전체 노드를 재귀적으로 순회 (내부 헬퍼)
     */
    function _walkAllNodes(tree, callback)
    {
        var rootId = tree.getRootNodeId();

        (function walk(nodeId)
        {
            var childIds = tree.getChildIds(nodeId) || [];

            childIds.forEach(function(childId)
            {
                callback(childId);
                walk(childId);
            });
        })(rootId);
    }

    /**
     * 전체 노드 펼치기
     * 사용법 : ComTree.expandAll('docFolderTree')
     */
    function expandAll(treeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return;
        }

        _walkAllNodes(tree, function(nodeId)
        {
            tree.open(nodeId);
        });
    }

    /**
     * 전체 노드 접기
     * ※ 최상위(1뎁스) 노드는 열린 상태로 남겨두고, 그 하위 노드들만 접는다
     *   (다 접어버리면 최상위 카테고리 자체가 안 보이게 되는 것 방지)
     * 사용법 : ComTree.collapseAll('docFolderTree')
     */
    function collapseAll(treeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return;
        }

        var rootId = tree.getRootNodeId();

        _walkAllNodes(tree, function(nodeId)
        {
            var isTopLevel = (tree.getParentId(nodeId) === rootId);

            if(!isTopLevel)
            {
                tree.close(nodeId);
            }
        });
    }

    /**
     * 데이터는 그대로 두고 화면(DOM)만 다시 그림
     * 사용법 : ComTree.refresh('menuTree')
     */
    function refresh(treeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        if(typeof tree.refresh === 'function')
        {
            tree.refresh();
        }

        return true;
    }

    /**
     * 트리 데이터 새로고침 - 이미 중첩된(nested) tree 구조 데이터로 교체
     * (인스턴스를 destroy/재생성하지 않고 데이터만 교체 → 핸들러/컨텍스트메뉴 유지됨)
     *
     * 사용법 : ComTree.reload('menuTree', nestedTreeData)
     */
    function reload(treeId, newNestedData)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return false;
        }

        var opts = _optionsMap[treeId] || {};
        var finalData = newNestedData;

        // register 때 rootLabel을 썼다면 reload 시에도 동일하게 씌워준다
        if(opts.rootLabel)
        {
            finalData =
            [
                {
                    text    : opts.rootLabel,
                    id      : opts.rootId || '__ROOT__',
                    children: newNestedData
                }
            ];
        }

        tree.resetAllData(finalData);
        _rebuildIdMap(treeId);

        return true;
    }

    /**
     * 트리 데이터 새로고침 - 오라클 등에서 다시 조회한 평면(flat) 리스트로 교체
     * register 시 넘겼던 fieldMap/rootParentId를 그대로 재사용한다.
     *
     * 사용법 :
     *   $.ajax({ url: '/api/menu/list' }).done(function(response)
     *   {
     *       ComTree.reloadFlatData('menuTree', response.data);
     *   });
     */
    function reloadFlatData(treeId, newFlatData)
    {
        var opts = _optionsMap[treeId] || {};
        var nestedData = convertFlatToTree(newFlatData, opts.fieldMap, opts.rootParentId);

        return reload(treeId, nestedData);
    }

    /**
     * 커스텀 id(서버 PK)로 트리 내부 nodeId 조회
     * 사용법 : ComTree.getNodeIdByCustomId('docFolderTree', 'FOLDER_001')
     */
    function getNodeIdByCustomId(treeId, customId)
    {
        if(!_idMap[treeId] || _idMap[treeId][customId] === undefined)
        {
            console.warn('ComTree: 매핑되지 않은 customId 입니다 → ' + customId);
            return null;
        }

        return _idMap[treeId][customId];
    }

    /**
     * 현재 선택된 노드의 내부 nodeId 조회
     * 사용법 : ComTree.getSelectedNodeId('docFolderTree')
     */
    function getSelectedNodeId(treeId)
    {
        var tree = getTree(treeId);

        if(!tree)
        {
            return null;
        }

        return tree.getSelectedNodeId();
    }

    /**
     * 버튼 등에서 호출 - 컨텍스트메뉴의 "하위 항목 추가"와 동일한 흐름
     * (모달 입력 → 트리 반영 → register 시 등록한 onAddChild 콜백)
     *
     * @param {string} treeId
     * @param {string|number} [parentNodeId] - 생략하면 현재 선택된 노드를 부모로 사용
     */
    function triggerAdd(treeId, parentNodeId)
    {
        var handlers = _handlersMap[treeId];

        if(!handlers)
        {
            console.warn('ComTree: contextMenu 핸들러가 등록되지 않았습니다 → ' + treeId);
            return;
        }

        var targetParentId = parentNodeId || getSelectedNodeId(treeId);

        if(!targetParentId)
        {
            _alertFallback('상위 항목을 먼저 선택해주세요.');
            return;
        }

        _handleAddChild(treeId, targetParentId, handlers);
    }

    /**
     * 버튼 등에서 호출 - 컨텍스트메뉴의 "이름 수정"과 동일한 흐름
     *
     * @param {string} treeId
     * @param {string|number} [nodeId] - 생략하면 현재 선택된 노드를 대상으로 사용
     */
    function triggerEdit(treeId, nodeId)
    {
        var handlers = _handlersMap[treeId];

        if(!handlers)
        {
            console.warn('ComTree: contextMenu 핸들러가 등록되지 않았습니다 → ' + treeId);
            return;
        }

        var targetNodeId = nodeId || getSelectedNodeId(treeId);

        if(!targetNodeId)
        {
            _alertFallback('수정할 항목을 먼저 선택해주세요.');
            return;
        }

        _handleEdit(treeId, targetNodeId, handlers);
    }

    /**
     * 버튼 등에서 호출 - 컨텍스트메뉴의 "삭제"와 동일한 흐름
     *
     * @param {string} treeId
     * @param {string|number} [nodeId] - 생략하면 현재 선택된 노드를 대상으로 사용
     */
    function triggerDelete(treeId, nodeId)
    {
        var handlers = _handlersMap[treeId];

        if(!handlers)
        {
            console.warn('ComTree: contextMenu 핸들러가 등록되지 않았습니다 → ' + treeId);
            return;
        }

        var targetNodeId = nodeId || getSelectedNodeId(treeId);

        if(!targetNodeId)
        {
            _alertFallback('삭제할 항목을 먼저 선택해주세요.');
            return;
        }

        _handleDelete(treeId, targetNodeId, handlers);
    }

    /**
     * 평면 리스트를 tui-tree 데이터 구조로 변환 (형제 노드 간 정렬 포함)
     *
     * @param {Array} flatList    - DB 조회 결과 (예: [{ menuId, upperMenuId, menuNm, menuOrder }, ...])
     * @param {object} fieldMap   - 실제 컬럼명 매핑
     *                              { id: 'menuId', parentId: 'upperMenuId', text: 'menuNm', order: 'menuOrder' }
     * @param {string|number} rootParentId - 최상위 메뉴의 upperMenuId 값 (보통 null 또는 '0')
     * @returns {Array} tui-tree data 옵션에 바로 넣을 수 있는 중첩 배열 (order 오름차순 정렬됨)
     */
    function convertFlatToTree(flatList, fieldMap, rootParentId)
    {
        fieldMap = fieldMap ||
        {
            id      : 'menuId',
            parentId: 'upperMenuId',
            text    : 'menuNm',
            order   : 'menuOrder'
        };

        rootParentId = (rootParentId === undefined) ? null : rootParentId;

        var idField     = fieldMap.id;
        var parentField = fieldMap.parentId;
        var textField   = fieldMap.text;
        var orderField  = fieldMap.order;   // 없으면 정렬 생략

        // 1) id 기준 맵 생성 + children 배열 미리 부여
        //    ※ id 중복 행은 첫 번째 것만 채택하고 콘솔에 경고 (같은 노드가 트리에 두 번 들어가는 것 방지)
        var nodeMap   = {};
        var seenId    = {};
        var cleanRows = [];

        flatList.forEach(function(row)
        {
            var id = row[idField];

            if(seenId[id])
            {
                console.warn('convertFlatToTree: 중복된 id(' + id + ') 발견 - 이 행은 무시합니다.', row);
                return;
            }

            seenId[id] = true;
            cleanRows.push(row);

            nodeMap[id] =
            {
                text    : row[textField],
                id      : id,
                children: [],
                raw     : row               // 원본 전체 보존 (필요 시 order 등 추가 필드 참조)
            };
        });

        // 2) 부모-자식 연결 (중복 제거된 cleanRows 기준으로만 순회)
        var tree = [];

        cleanRows.forEach(function(row)
        {
            var node     = nodeMap[row[idField]];
            var parentId = row[parentField];

            if(parentId === rootParentId || parentId === null || parentId === undefined)
            {
                tree.push(node);
            }
            else if(nodeMap[parentId])
            {
                nodeMap[parentId].children.push(node);
            }
            else
            {
                console.warn('convertFlatToTree: 상위(' + parentId + ')를 찾을 수 없는 노드 → 최상위로 편입', row);
                tree.push(node);
            }
        });

        // 3) order 기준 정렬 (형제 노드끼리) + 빈 children 정리
        //    ※ 이미 children이 정리된(undefined) 노드는 건너뛰는 방어 코드 포함
        (function sortAndCleanup(nodes)
        {
            if(orderField)
            {
                nodes.sort(function(a, b)
                {
                    return (a.raw[orderField] || 0) - (b.raw[orderField] || 0);
                });
            }

            nodes.forEach(function(node)
            {
                if(!node.children)
                {
                    return;   // 이미 정리됐거나 children이 없는 노드는 건너뜀 (방어 코드)
                }

                if(node.children.length === 0)
                {
                    delete node.children;
                }
                else
                {
                    sortAndCleanup(node.children);
                }
            });
        })(tree);

        return tree;
    }

    function destroy(treeId)
    {
        if(_trees[treeId])
        {
            _trees[treeId].destroy();
            delete _trees[treeId];
            delete _idMap[treeId];
            delete _handlersMap[treeId];
            delete _maxDepthMap[treeId];
            delete _optionsMap[treeId];
        }
    }

    return {
        register           : register,
        getTree            : getTree,
        addNode            : addNode,
        editNodeText       : editNodeText,
        editNodeData       : editNodeData,
        removeNode         : removeNode,
        moveNode           : moveNode,
        isLeaf             : isLeaf,
        getNodeIdByCustomId: getNodeIdByCustomId,
        getSelectedNodeId  : getSelectedNodeId,
        refresh            : refresh,
        reload             : reload,
        reloadFlatData     : reloadFlatData,
        expandAll          : expandAll,
        collapseAll        : collapseAll,
        triggerAdd         : triggerAdd,
        triggerEdit        : triggerEdit,
        triggerDelete      : triggerDelete,
        convertFlatToTree  : convertFlatToTree,
        destroy            : destroy
    };
})();