import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { BooleanInput } from '@angular/cdk/coercion';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from 'app/core/auth/auth.service';
import { User } from 'app/models/auth.models';
import { CommonModule, NgClass } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'user',
    templateUrl: './user.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    exportAs: 'user',
    imports: [CommonModule, RouterModule, MatMenuModule, MatIconModule, MatDividerModule],
})
export class UserComponent implements OnInit, OnDestroy {
    static ngAcceptInputType_showAvatar: BooleanInput;

    @Input() showAvatar: boolean = true;
    user: User;

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _router: Router,
        private _authService: AuthService
    ) {}

    ngOnInit(): void {
        // Subscribe to user changes
        this._authService.currentUser$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((user: User) => {
                this.user = user;
                this._changeDetectorRef.markForCheck();
            });
    }

    ngOnDestroy(): void {
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    updateUserStatus(status: string): void {
        // Return if user is not available
        if (!this.user) {
            return;
        }

        // Update the user status
        // Implementation depends on your user status system
    }

    signOut(): void {
        this._authService.signOut();
        this._router.navigate(['/sign-in']);
    }

    getUserDisplayName(): string {
        if (this.user) {
            return `${this.user.prenom} ${this.user.nom}`;
        }
        return 'Guest';
    }

    getUserAvatar(): string {
        // Return avatar URL or default
        return 'images/avatars/avatar-placeholder.jpg';
    }

    getPrimaryRole(): string {
        if (this.user?.roles?.length > 0) {
            // Remove ROLE_ prefix and format
            const role = this.user.roles[0].replace('ROLE_', '');
            return role.charAt(0).toUpperCase() + role.slice(1).toLowerCase().replace('_', ' ');
        }
        return 'User';
    }
}