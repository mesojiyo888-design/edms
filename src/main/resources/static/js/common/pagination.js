
/**

*/

(function(window) {
    function PaginationManager(targetId, totalItems, itemsPerPage, type) {
        this.target = document.getElementById(targetId);
        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        this.type = type || 'page';
        this.currentPage = 1;
        this.totalPages = Math.ceil(totalItems / itemsPerPage);
        this.onPageChange = null; // 외부 데이터 연동용 콜백
    }

    PaginationManager.prototype.updateConfig = function(newType, newItemsPerPage) {
        this.type = newType;
        if (newItemsPerPage) {
            this.itemsPerPage = newItemsPerPage;
            this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        }
        this.render();
    };

    PaginationManager.prototype.render = function() {
        if (!this.target) return;
        this.target.innerHTML = '';
        if (this.type === 'page') {
            this.renderPageNumbers();
        } else {
            this.renderLoadMore();
        }
    };

    PaginationManager.prototype.renderPageNumbers = function() {
        var self = this;
        var nav = document.createElement('div');
        nav.className = 'pagination';

        var createBtn = function(text, page) {
            var btn = document.createElement('button');
            btn.innerHTML = text;
            if (self.currentPage === page) btn.className = 'active';
            btn.onclick = function() {
                self.currentPage = page;
                self.render();
                if (self.onPageChange) self.onPageChange(self.currentPage);
            };
            return btn;
        };

        nav.appendChild(createBtn('<<', 1));
        nav.appendChild(createBtn('<', Math.max(1, this.currentPage - 1)));

        for (var i = 1; i <= this.totalPages; i++) {
            nav.appendChild(createBtn(i, i));
        }

        nav.appendChild(createBtn('>', Math.min(this.totalPages, this.currentPage + 1)));
        nav.appendChild(createBtn('>>', this.totalPages));

        this.target.appendChild(nav);
    };

    PaginationManager.prototype.renderLoadMore = function() {
        var self = this;
        if (this.currentPage < this.totalPages) {
            var btn = document.createElement('button');
            btn.innerHTML = '더보기';
            btn.onclick = function() {
                self.currentPage++;
                if (self.onPageChange) self.onPageChange(self.currentPage);
            };
            this.target.appendChild(btn);
        }
    };

    // 전역 객체에 노출
    window.PaginationManager = PaginationManager;
})(window);