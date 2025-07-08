import { FuseNavigationItem } from '@fuse/components/navigation';

export const defaultNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboard',
        title: 'Dashboard',
        subtitle: 'Overview and statistics',
        type: 'basic',
        icon: 'heroicons_outline:home',
        link: '/dashboard'
    },
    {
        id: 'plans',
        title: 'Action Plans',
        subtitle: 'Manage strategic plans',
        type: 'collapsable',
        icon: 'heroicons_outline:document-text',
        children: [
            {
                id: 'plans.list',
                title: 'All Plans',
                type: 'basic',
                icon: 'heroicons_outline:list-bullet',
                link: '/plans'
            },
            {
                id: 'plans.create',
                title: 'Create Plan',
                type: 'basic',
                icon: 'heroicons_outline:plus',
                link: '/plans/create'
            },
            {
                id: 'plans.dashboard',
                title: 'Plans Dashboard',
                type: 'basic',
                icon: 'heroicons_outline:chart-bar',
                link: '/plans/dashboard'
            }
        ]
    },
    {
        id: 'variables',
        title: 'Action Variables',
        subtitle: 'Manage plan variables',
        type: 'basic',
        icon: 'heroicons_outline:variable',
        link: '/variables'
    },
    {
        id: 'exercises',
        title: 'Exercises',
        subtitle: 'Yearly exercises',
        type: 'basic',
        icon: 'heroicons_outline:calendar',
        link: '/exercises',
        meta: {
            roles: ['ADMINISTRATEUR', 'DIRECTEUR_GENERAL']
        }
    },
    {
        id: 'divider-1',
        type: 'divider'
    },
    {
        id: 'admin',
        title: 'Administration',
        subtitle: 'System management',
        type: 'group',
        icon: 'heroicons_outline:cog-6-tooth',
        meta: {
            roles: ['ADMINISTRATEUR']
        },
        children: [
            {
                id: 'admin.users',
                title: 'User Management',
                type: 'basic',
                icon: 'heroicons_outline:users',
                link: '/admin/users'
            },
            {
                id: 'admin.profiles',
                title: 'Profile Management',
                type: 'basic',
                icon: 'heroicons_outline:identification',
                link: '/admin/profiles'
            },
            {
                id: 'admin.servicelines',
                title: 'Service Lines',
                type: 'basic',
                icon: 'heroicons_outline:building-office',
                link: '/admin/service-lines'
            },
            {
                id: 'admin.audit',
                title: 'Audit Logs',
                type: 'basic',
                icon: 'heroicons_outline:eye',
                link: '/admin/audit'
            }
        ]
    },
    {
        id: 'divider-2',
        type: 'divider'
    },
    {
        id: 'notifications',
        title: 'Notifications',
        subtitle: 'System alerts',
        type: 'basic',
        icon: 'heroicons_outline:bell',
        link: '/notifications',
        badge: {
            title: '3',
            classes: 'px-2 bg-pink-600 text-white rounded-full'
        }
    },
    {
        id: 'profile',
        title: 'Profile',
        subtitle: 'Account settings',
        type: 'basic',
        icon: 'heroicons_outline:user-circle',
        link: '/profile'
    }
];

export const compactNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboard',
        title: 'Dashboard',
        type: 'basic',
        icon: 'heroicons_outline:home',
        link: '/dashboard'
    },
    {
        id: 'plans',
        title: 'Plans',
        type: 'basic',
        icon: 'heroicons_outline:document-text',
        link: '/plans'
    },
    {
        id: 'variables',
        title: 'Variables',
        type: 'basic',
        icon: 'heroicons_outline:variable',
        link: '/variables'
    },
    {
        id: 'admin',
        title: 'Admin',
        type: 'basic',
        icon: 'heroicons_outline:cog-6-tooth',
        link: '/admin',
        meta: {
            roles: ['ADMINISTRATEUR']
        }
    }
];

export const futuristicNavigation: FuseNavigationItem[] = defaultNavigation;
export const horizontalNavigation: FuseNavigationItem[] = defaultNavigation;