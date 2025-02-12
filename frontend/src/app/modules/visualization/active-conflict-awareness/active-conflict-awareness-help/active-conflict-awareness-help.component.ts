import { Component } from '@angular/core';
import { MatCard } from "@angular/material/card";
import { MatAccordion, MatExpansionPanel, MatExpansionPanelHeader } from "@angular/material/expansion";

@Component({
  selector: 'app-active-conflict-awareness-help',
  standalone: true,
  imports: [
    MatCard,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader
  ],
  templateUrl: './active-conflict-awareness-help.component.html',
  styleUrl: './active-conflict-awareness-help.component.scss'
})
export class ActiveConflictAwarenessHelpComponent {

}
