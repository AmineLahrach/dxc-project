import { inject } from '@angular/core';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { MessagesService } from 'app/layout/common/messages/messages.service';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { QuickChatService } from 'app/layout/common/quick-chat/quick-chat.service';
import { ShortcutsService } from 'app/layout/common/shortcuts/shortcuts.service';
import { AuthService } from 'app/core/auth/auth.service';
import { forkJoin, of } from 'rxjs';

export const initialDataResolver = () => {
    const navigationService = inject(NavigationService);
    const messagesService = inject(MessagesService);
    const notificationsService = inject(NotificationsService);
    const quickChatService = inject(QuickChatService);
    const shortcutsService = inject(ShortcutsService);
    const authService = inject(AuthService);

    // Get current user from storage
    const currentUser = authService.getCurrentUserFromStorage();
    const userRoles = currentUser?.roles || [];

    // Fork join multiple API endpoint calls to wait all of them to finish
    return forkJoin([
        navigationService.getAllByUserRoles(userRoles),
        messagesService.getAll(),
        notificationsService.getAll(),
        quickChatService.getChats(),
        shortcutsService.getAll(),
    ]);
};
