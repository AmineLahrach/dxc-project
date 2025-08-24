export interface Notification {
    id: Number;
    oldId: string;
    type?: string;
    image?: string;
    titre?: string;
    contenu?: string;
    date: string;
    link?: string;
    useRouter?: boolean;
    recu: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}