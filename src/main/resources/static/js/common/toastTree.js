/**
 * toastTree.js
 *
 * 역할:
 * - Toast UI Tree 생성
 * - 트리 인스턴스 관리
 * - 선택 노드 관리
 * - 노드 추가
 * - 하위 노드 추가
 * - 노드 제거
 * - 컨텍스트 메뉴 처리
 * - 버튼/모달 이벤트 연결
 *
 * 전제:
 * - tui-tree.js는 이미 include 되어 있어야 함
 * - ContextMenu를 쓸 경우 tui-context-menu.js가 tui-tree.js보다 먼저 include 되어 있어야 함
 */
var ToastTree = (function() {
    var instances = {};

    function getItem(treeId) {
        var item = instances[treeId];

        if (!item) {
            throw new Error('ToastTree 인스턴스가 없습니다. treeId=' + treeId);
        }

        return item;
    }

    function getEventNamespace(treeId) {
        return '.toastTree_' + String(treeId).replace(/[^a-zA-Z0-9_]/g, '_');
    }

    function getText(item, nodeId) {
        if (nodeId === null || nodeId === undefined) {
            return '';
        }

        var nodeData = item.tree.getNodeData(nodeId);
        return nodeData ? nodeData.text : '';
    }

    function updateSelectedValue(item) {
        var ui = item.ui;

        if (!ui || !ui.selectedValue) {
            return;
        }

        if (item.selectedNodeId === null) {
            $(ui.selectedValue).val('');
            return;
        }

        $(ui.selectedValue).val('selected : ' + getText(item, item.selectedNodeId));
    }

    function setSelectedNode(item, nodeId) {
        item.selectedNodeId = nodeId || null;
        updateSelectedValue(item);
    }

    function clearSelectedNode(item) {
        if (item.selectedNodeId === null) {
            return;
        }

        if (typeof item.tree.deselect === 'function') {
            item.tree.deselect();
        }

        setSelectedNode(item, null);
    }

    function openModal(selector) {
        $(selector).css('display', 'flex');
    }

    function closeModal(selector) {
        $(selector).hide();
    }

    function getNodeDepth(item, nodeId) {
        if (nodeId === null || nodeId === undefined) {
            return 0;
        }

        return item.tree.getDepth(nodeId);
    }

    function canAddChildNode(item, nodeId) {
        if (item.options.useDepthLimit === false) {
            return true;
        }

        //return getNodeDepth(item, nodeId) < item.options.maxDepth;
        return true; //제한없도록 수정
    }

    function openAddModal(item, mode, targetNodeId) {
        var ui = item.ui;

        item.addMode = mode;
        item.addTargetNodeId = targetNodeId || null;

        $(ui.nodeNameInput).val('');
        openModal(ui.addNodeModal);
        $(ui.nodeNameInput).focus();
    }

    function openRemoveModal(item, targetNodeId) {
        var ui = item.ui;

        item.removeTargetNodeId = targetNodeId;

        $(ui.removeNodeText).text('삭제 대상 : ' + getText(item, targetNodeId));
        openModal(ui.removeNodeModal);
    }

    function addSiblingNode(item, nodeName) {
        var tree = item.tree;
        var addedNodeIds;
        var addedNodeId;

        /*
         * 미선택 상태:
         * 화면에 보이는 최상위 노드 목록의 제일 하단에 추가
         */
        if (item.addTargetNodeId === null) {
            addedNodeIds = tree.add({
                text: nodeName
            }, item.rootNodeId);

            return addedNodeIds[0];
        }

        /*
         * 선택 상태:
         * 선택한 노드 바로 아래, 같은 뎁스에 추가
         */
        var parentNodeId = tree.getParentId(item.addTargetNodeId);
        var targetIndex = tree.getNodeIndex(item.addTargetNodeId);

        addedNodeIds = tree.add({
            text: nodeName
        }, parentNodeId);

        addedNodeId = addedNodeIds[0];

        /*
         * tree.add는 기본적으로 부모의 마지막 자식으로 추가됨.
         * 선택 노드 바로 아래로 이동시키기 위해 move 사용.
         */
        tree.move(addedNodeId, parentNodeId, targetIndex + 1);

        return addedNodeId;
    }

    function addChildNode(item, nodeName) {
        var tree = item.tree;

        if (item.addTargetNodeId === null) {
            alert('하위 노드를 추가할 노드를 선택하세요.');
            return null;
        }

        if (!canAddChildNode(item, item.addTargetNodeId)) {
            alert('최대 ' + item.options.maxDepth + '뎁스까지만 추가할 수 있습니다.');
            return null;
        }

        var addedNodeIds = tree.add({
            text: nodeName
        }, item.addTargetNodeId);

        var addedNodeId = addedNodeIds[0];

        /*
         * 부모 노드를 열어서 추가한 하위 노드가 보이게 함
         */
        tree.open(item.addTargetNodeId);

        return addedNodeId;
    }

    function confirmAddNode(item) {
        var ui = item.ui;
        var nodeName = $.trim($(ui.nodeNameInput).val());

        if (nodeName === '') {
            alert('노드 이름을 입력하세요.');
            $(ui.nodeNameInput).focus();
            return;
        }

        var addedNodeId;

        if (item.addMode === 'child') {
            addedNodeId = addChildNode(item, nodeName);
        } else {
            addedNodeId = addSiblingNode(item, nodeName);
        }

        if (addedNodeId !== null && addedNodeId !== undefined) {
            item.tree.select(addedNodeId);
            setSelectedNode(item, addedNodeId);
        }

        closeModal(ui.addNodeModal);
        ToastTree.syncHiddenData(item.treeId);
    }

    function confirmRemoveNode(item) {
        var ui = item.ui;

        if (item.removeTargetNodeId === null) {
            closeModal(ui.removeNodeModal);
            return;
        }

        item.tree.remove(item.removeTargetNodeId);

        if (item.selectedNodeId === item.removeTargetNodeId) {
            setSelectedNode(item, null);
        }

        item.removeTargetNodeId = null;

        closeModal(ui.removeNodeModal);
        ToastTree.syncHiddenData(item.treeId);
    }

    function serializeNode(item, nodeId) {
        var tree = item.tree;
        var nodeData = tree.getNodeData(nodeId) || {};
        var childIds = tree.getChildIds(nodeId) || [];
        var result = {};

        /*
         * children은 실제 트리 구조 기준으로 다시 만들기 때문에 제외
         */
        $.each(nodeData, function(key, value) {
            if (key !== 'children') {
                result[key] = value;
            }
        });

        if (childIds.length > 0) {
            result.children = [];

            $.each(childIds, function(index, childId) {
                result.children.push(serializeNode(item, childId));
            });
        }

        return result;
    }

    function bindTreeEvents(item) {
        var tree = item.tree;

        /*
         * 노드 선택
         */
        tree.on('select', function(eventData) {
            setSelectedNode(item, eventData.nodeId);
        });

        /*
         * 노드 선택 해제
         */
        tree.on('deselect', function(eventData) {
            if (item.selectedNodeId === eventData.nodeId) {
                setSelectedNode(item, null);
            }
        });

        /*
         * 우클릭 메뉴를 열기 전에 우클릭한 노드를 선택 처리
         */
        tree.on('beforeOpenContextMenu', function(eventData) {
            tree.select(eventData.nodeId);
            setSelectedNode(item, eventData.nodeId);
        });

        /*
         * 우클릭 메뉴 선택
         */
        tree.on('selectContextMenu', function(eventData) {
            var command = eventData.command;
            var nodeId = eventData.nodeId;

            tree.select(nodeId);
            setSelectedNode(item, nodeId);

            if (command === 'addNode') {
                openAddModal(item, 'sibling', nodeId);
                return;
            }

            if (command === 'addChildNode') {
                if (!canAddChildNode(item, nodeId)) {
                    alert('최대 ' + item.options.maxDepth + '뎁스까지만 추가할 수 있습니다.');
                    return;
                }

                openAddModal(item, 'child', nodeId);
                return;
            }

            if (command === 'removeNode') {
                openRemoveModal(item, nodeId);
                return;
            }
        });
    }

    return {
        /**
         * ToastTree 초기화
         *
         * @param treeId
         *        트리가 그려질 div id
         *
         * @param data
         *        트리 데이터
         *
         * @param options
         *        트리 옵션
         */
        init: function(treeId, data, options) {
            options = options || {};

            var el = document.getElementById(treeId);

            if (!el) {
                throw new Error('트리 영역을 찾을 수 없습니다. treeId=' + treeId);
            }

            if (!window.tui || !window.tui.Tree) {
                throw new Error('tui.Tree가 없습니다. tui-tree.js include 여부를 확인하세요.');
            }

            /*
             * 같은 treeId로 다시 init하면 기존 인스턴스 제거
             * 이벤트 중복 등록 방지 목적
             */
            if (instances[treeId]) {
                this.destroy(treeId);
            }

            var treeOptions = $.extend(true, {
                data: data || [],
                nodeDefaultState: 'opened',
                usageStatistics: false
            }, options.treeOptions || {});

            treeOptions.data = data || [];

            var tree = new tui.Tree('#' + treeId, treeOptions);

            /*
             * 선택 기능
             */
            if (options.selectable !== false) {
                tree.enableFeature('Selectable', {
                    selectedClassName: options.selectedClassName || 'tui-tree-selected'
                });
            }

            /*
             * 컨텍스트 메뉴 기능
             */
            if (options.useContextMenu !== false) {
                if (!window.tui.ContextMenu || typeof window.tui.ContextMenu !== 'function') {
                    console.warn('tui.ContextMenu가 없습니다. tui-context-menu.js 로드 순서를 확인하세요.');
                } else {
                    tree.enableFeature('ContextMenu', {
                        menuData: options.menuData || [
                            { title: '노드 추가', command: 'addNode' },
                            { title: '하위 노드 추가', command: 'addChildNode' },
                            { title: '노드 제거', command: 'removeNode' }
                        ]
                    });
                }
            }

            instances[treeId] = {
                treeId: treeId,
                tree: tree,
                rootNodeId: tree.getRootNodeId(),
                options: $.extend(true, {
                    maxDepth: 6,
                    useDepthLimit: true,
                    deselectOnOutsideClick: true
                }, options || {}),
                ui: null,
                selectedNodeId: null,
                addMode: null,
                addTargetNodeId: null,
                removeTargetNodeId: null,
                eventNs: getEventNamespace(treeId)
            };

            bindTreeEvents(instances[treeId]);

            return tree;
        },

        /**
         * 버튼, 모달 이벤트 연결
         *
         * ToastGrid.bindColumnData(...)처럼
         * init 이후 화면별 요소만 연결하는 함수
         */
        bindButtons: function(treeId, uiOptions) {
            var item = getItem(treeId);
            var ns = item.eventNs;

            item.ui = $.extend(true, {
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
            }, uiOptions || {});

            var ui = item.ui;

            /*
             * 이벤트 중복 방지:
             * off 후 on
             */
            $(ui.addNodeBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();

                /*
                 * 미선택:
                 * - 최상위 최하단 추가
                 *
                 * 선택:
                 * - 선택 노드 바로 아래 같은 뎁스 추가
                 */
                openAddModal(item, 'sibling', item.selectedNodeId);
            });

            $(ui.addChildNodeBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();

                if (item.selectedNodeId === null) {
                    alert('하위 노드를 추가할 노드를 선택하세요.');
                    return;
                }

                if (!canAddChildNode(item, item.selectedNodeId)) {
                    alert('최대 ' + item.options.maxDepth + '뎁스까지만 추가할 수 있습니다.');
                    return;
                }

                openAddModal(item, 'child', item.selectedNodeId);
            });

            $(ui.removeNodeBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();

                if (item.selectedNodeId === null) {
                    alert('삭제할 노드를 선택하세요.');
                    return;
                }

                openRemoveModal(item, item.selectedNodeId);
            });

            $(ui.addConfirmBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();
                confirmAddNode(item);
            });

            $(ui.addCancelBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();
                closeModal(ui.addNodeModal);
            });

            $(ui.nodeNameInput).off('keyup' + ns).on('keyup' + ns, function(e) {
                if (e.key === 'Enter' || e.keyCode === 13) {
                    confirmAddNode(item);
                }
            });

            $(ui.removeConfirmBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();
                confirmRemoveNode(item);
            });

            $(ui.removeCancelBtn).off('click' + ns).on('click' + ns, function(e) {
                e.preventDefault();
                item.removeTargetNodeId = null;
                closeModal(ui.removeNodeModal);
            });

            /*
             * 노드/버튼 외 클릭 시 선택 해제
             */
            $(document).off('click' + ns).on('click' + ns, function(e) {
                if (item.options.deselectOnOutsideClick === false) {
                    return;
                }

                var target = e.target;

                /*
                 * 버튼 클릭이면 선택 유지
                 */
                if ($(target).closest('button').length > 0) {
                    return;
                }

                /*
                 * 트리 노드 클릭이면 선택 유지
                 */
                if ($(target).closest('#' + treeId + ' .tui-tree-node').length > 0) {
                    return;
                }

                /*
                 * 컨텍스트 메뉴 클릭이면 선택 유지
                 */
                if ($(target).closest('.tui-contextmenu, .tui-context-menu, .tui-contextmenu-root').length > 0) {
                    return;
                }

                clearSelectedNode(item);
            });

            /*
             * 최초 hidden data 동기화
             */
            this.syncHiddenData(treeId);
        },

        /**
         * 전체 트리 데이터 반환
         */
        getData: function(treeId) {
            var item = getItem(treeId);
            var rootChildren = item.tree.getChildIds(item.rootNodeId) || [];
            var result = [];

            $.each(rootChildren, function(index, nodeId) {
                result.push(serializeNode(item, nodeId));
            });

            return result;
        },

        /**
         * 선택된 노드 데이터 반환
         */
        getSelectedData: function(treeId) {
            var item = getItem(treeId);

            if (item.selectedNodeId === null) {
                return null;
            }

            return item.tree.getNodeData(item.selectedNodeId);
        },

        /**
         * 선택된 노드 ID 반환
         */
        getSelectedNodeId: function(treeId) {
            return getItem(treeId).selectedNodeId;
        },

        /**
         * 원본 tui.Tree 객체 반환
         */
        getTree: function(treeId) {
            return getItem(treeId).tree;
        },

        /**
         * hidden input에 현재 트리 JSON 저장
         */
        syncHiddenData: function(treeId) {
            var item = getItem(treeId);

            if (!item.ui || !item.ui.hiddenData) {
                return;
            }

            var $hidden = $(item.ui.hiddenData);

            if ($hidden.length === 0) {
                return;
            }

            $hidden.val(JSON.stringify(this.getData(treeId)));
        },

        /**
         * 인스턴스 제거
         */
        destroy: function(treeId) {
            var item = instances[treeId];

            if (!item) {
                return;
            }

            $(document).off(item.eventNs);

            if (item.ui) {
                $(item.ui.addNodeBtn).off(item.eventNs);
                $(item.ui.addChildNodeBtn).off(item.eventNs);
                $(item.ui.removeNodeBtn).off(item.eventNs);
                $(item.ui.addConfirmBtn).off(item.eventNs);
                $(item.ui.addCancelBtn).off(item.eventNs);
                $(item.ui.nodeNameInput).off(item.eventNs);
                $(item.ui.removeConfirmBtn).off(item.eventNs);
                $(item.ui.removeCancelBtn).off(item.eventNs);
            }

            /*
             * Toast UI Tree 버전에 따라 destroy가 없을 수 있으므로 방어
             */
            if (item.tree && typeof item.tree.destroy === 'function') {
                item.tree.destroy();
            }

            delete instances[treeId];
        }
    };
})();