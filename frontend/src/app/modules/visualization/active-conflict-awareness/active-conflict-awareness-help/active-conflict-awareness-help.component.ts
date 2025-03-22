import { Component } from '@angular/core';
import { MatDialogContent } from "@angular/material/dialog";
import { MatCard, MatCardContent, MatCardHeader } from "@angular/material/card";

@Component({
  selector: 'app-active-conflict-awareness-help',
  standalone: true,
  imports: [
    MatDialogContent,
    MatCard,
    MatCardHeader,
    MatCardContent
  ],
  templateUrl: './active-conflict-awareness-help.component.html',
  styleUrl: './active-conflict-awareness-help.component.scss'
})
export class ActiveConflictAwarenessHelpComponent {

}
